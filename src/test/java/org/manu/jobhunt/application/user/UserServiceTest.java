package org.manu.jobhunt.application.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.application.user.command.SignInCommand;
import org.manu.jobhunt.application.user.exception.SignInNotInvitedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for UserService: who may sign in, and which user they are.
 */
class UserServiceTest {

    private final UserService invitationOnly = new UserService("Alice@Example.com, bob@example.com", "Alice-Dev, bob", false);

    @Test
    @DisplayName("""
            Given an invited email in any letter case
            When its owner signs in
            Then they're allowed
            """)
    void anInvitedEmailIsAllowed() {
        assertDoesNotThrow(() -> invitationOnly.requireAllowed(signIn("alice@EXAMPLE.com", true)));
    }

    @Test
    @DisplayName("""
            Given an email that isn't on the list
            When its owner signs in
            Then they're refused
            """)
    void anUninvitedEmailIsRefused() {
        assertThrows(SignInNotInvitedException.class,
                () -> invitationOnly.requireAllowed(signIn("stranger@example.com", true)));
    }

    @Test
    @DisplayName("""
            Given an invited email the provider hasn't verified
            When someone signs in with it
            Then they're refused: an unverified email proves nothing
            """)
    void anUnverifiedEmailIsRefused() {
        assertThrows(SignInNotInvitedException.class,
                () -> invitationOnly.requireAllowed(signIn("alice@example.com", false)));
    }

    @Test
    @DisplayName("""
            Given an empty list
            When anyone signs in
            Then they're refused: a missing setting lets nobody in
            """)
    void anEmptyListLetsNobodyIn() {
        final UserService nobody = new UserService("", "", false);

        assertThrows(SignInNotInvitedException.class, () -> nobody.requireAllowed(signIn("alice@example.com", true)));
    }

    @Test
    @DisplayName("""
            Given the app is open to everyone
            When anyone signs in
            Then they're allowed
            """)
    void anOpenAppLetsAnyoneIn() {
        final UserService open = new UserService("", "", true);

        assertDoesNotThrow(() -> open.requireAllowed(signIn("stranger@example.com", true)));
    }

    @Test
    @DisplayName("""
            Given the same provider account on two sign-ins, even with a changed email
            When its user id is worked out
            Then it's the same user, and another account is a different one
            """)
    void theUserIdFollowsTheProviderAccount() {
        // Given
        final SignInCommand first = SignInCommand.builder().provider("google").providerUserId("1").email("a@x").build();
        final SignInCommand later = SignInCommand.builder().provider("google").providerUserId("1").email("b@x").build();
        final SignInCommand other = SignInCommand.builder().provider("google").providerUserId("2").email("a@x").build();

        // When / Then
        assertEquals(invitationOnly.userIdOf(first), invitationOnly.userIdOf(later));
        assertNotEquals(invitationOnly.userIdOf(first), invitationOnly.userIdOf(other));
    }

    @Test
    @DisplayName("""
            Given an invited GitHub username in any letter case
            When its owner signs in
            Then they're allowed
            """)
    void anInvitedGithubUserIsAllowed() {
        assertDoesNotThrow(() -> invitationOnly.requireAllowed(gitHubSignIn("alice-DEV", null)));
    }

    @Test
    @DisplayName("""
            Given a GitHub username that isn't on the list, with an email that is
            When its owner signs in
            Then they're refused: GitHub goes by username, and its email isn't verified here
            """)
    void aGithubUserIsNotInvitedByEmail() {
        assertThrows(SignInNotInvitedException.class,
                () -> invitationOnly.requireAllowed(gitHubSignIn("stranger", "alice@example.com")));
    }

    @Test
    @DisplayName("""
            Given an invited GitHub username, signing in through another provider
            When they sign in
            Then they're refused: the username only counts on GitHub
            """)
    void aGithubInvitationOnlyCountsOnGithub() {
        // Given
        final SignInCommand command = SignInCommand.builder()
                .provider("google").providerUserId("1").username("alice-dev").email("stranger@example.com")
                .emailVerified(true).build();

        // When / Then
        assertThrows(SignInNotInvitedException.class, () -> invitationOnly.requireAllowed(command));
    }

    @Test
    @DisplayName("""
            Given a GitHub account that changed its username
            When its user id is worked out
            Then it's the same user, and it differs from a Google account with the same number
            """)
    void theUserIdFollowsTheGithubAccount() {
        // Given
        final SignInCommand before = SignInCommand.builder().provider("github").providerUserId("1").username("old").build();
        final SignInCommand after = SignInCommand.builder().provider("github").providerUserId("1").username("new").build();
        final SignInCommand google = SignInCommand.builder().provider("google").providerUserId("1").build();

        // When / Then
        assertEquals(invitationOnly.userIdOf(before), invitationOnly.userIdOf(after));
        assertNotEquals(invitationOnly.userIdOf(before), invitationOnly.userIdOf(google));
    }

    private SignInCommand gitHubSignIn(String username, String email) {
        return SignInCommand.builder().provider("github").providerUserId("7").username(username).email(email).build();
    }

    private SignInCommand signIn(String email, boolean verified) {
        return SignInCommand.builder().provider("google").providerUserId("1").email(email).emailVerified(verified).build();
    }
}
