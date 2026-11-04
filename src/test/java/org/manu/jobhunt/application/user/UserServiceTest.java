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

    private final UserService invitationOnly = new UserService("Alice@Example.com, bob@example.com", false);

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
        final UserService nobody = new UserService("", false);

        assertThrows(SignInNotInvitedException.class, () -> nobody.requireAllowed(signIn("alice@example.com", true)));
    }

    @Test
    @DisplayName("""
            Given the app is open to everyone
            When anyone signs in
            Then they're allowed
            """)
    void anOpenAppLetsAnyoneIn() {
        final UserService open = new UserService("", true);

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

    private SignInCommand signIn(String email, boolean verified) {
        return SignInCommand.builder().provider("google").providerUserId("1").email(email).emailVerified(verified).build();
    }
}
