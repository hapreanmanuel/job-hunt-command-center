package org.manu.jobhunt.integration.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.application.user.UserApplication;
import org.manu.jobhunt.application.user.command.SignInCommand;
import org.manu.jobhunt.application.user.exception.SignInNotInvitedException;
import org.manu.jobhunt.integration.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What signing in writes, once the provider has confirmed who someone is.
 */
class SignInTest extends AbstractIntegrationTest {

    @Autowired
    private UserApplication userApplication;

    @Test
    @DisplayName("""
            Given an invited person signing in for the first time
            When they sign in, and later sign in again
            Then they're the same user, and their row exists once
            """)
    void signInRegistersANewUserOnce() {
        // When
        final UUID first = userApplication.signIn(signInAs("invited@example.com"));
        final UUID second = userApplication.signIn(signInAs("invited@example.com"));

        // Then
        assertEquals(first, second);
        assertEquals(1, countUsers(first));
    }

    @Test
    @DisplayName("""
            Given someone who hasn't been invited
            When they sign in
            Then they're refused and nothing is written
            """)
    void signInWritesNothingForAStranger() {
        // Given
        final int before = DATABASE.queryForObject("SELECT count(*) FROM app.app_user", Integer.class);

        // When / Then
        assertThrows(SignInNotInvitedException.class, () -> userApplication.signIn(signInAs("stranger@example.com")));
        assertEquals(before, DATABASE.queryForObject("SELECT count(*) FROM app.app_user", Integer.class));
    }

    @Test
    @DisplayName("""
            Given an invited GitHub user
            When they sign in
            Then their user row exists
            """)
    void signInRegistersAGithubUser() {
        // When
        final UUID user = userApplication.signIn(SignInCommand.builder()
                .provider("github").providerUserId("42").username("invited-user")
                .build());

        // Then
        assertEquals(1, countUsers(user));
    }

    private SignInCommand signInAs(String email) {
        return SignInCommand.builder()
                .provider("google").providerUserId(email).email(email).emailVerified(true)
                .build();
    }

    private int countUsers(UUID id) {
        return DATABASE.queryForObject("SELECT count(*) FROM app.app_user WHERE id = ?", Integer.class, id);
    }
}
