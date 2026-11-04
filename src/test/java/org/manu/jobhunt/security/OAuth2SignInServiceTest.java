package org.manu.jobhunt.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.application.user.UserApplication;
import org.manu.jobhunt.application.user.command.SignInCommand;
import org.manu.jobhunt.application.user.exception.SignInNotInvitedException;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for OAuth2SignInService: what happens after Google confirmed who signed in. Every
 * failure must be an OAuth2AuthenticationException, the only kind Spring Security turns into the
 * redirect back to the sign-in page.
 */
class OAuth2SignInServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");

    private final UserApplication userApplication = mock(UserApplication.class);
    private final OAuth2SignInService signInService = new OAuth2SignInService(
            request -> new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                    Map.of("sub", "google-123", "email", "alice@example.com", "email_verified", true),
                    "sub"),
            userApplication);

    @Test
    @DisplayName("""
            Given Google confirmed an invited user
            When the sign-in hook runs
            Then the session keeps only our own user id, and what Google said was passed on
            """)
    void loadUserReturnsTheSignedInUser() {
        // Given
        when(userApplication.signIn(any())).thenReturn(USER_ID);

        // When
        final OAuth2User user = signInService.loadUser(request());

        // Then
        assertEquals(USER_ID, ((SignedInUser) user).getUserId());
        assertEquals(Map.of("userId", USER_ID.toString()), user.getAttributes());

        final ArgumentCaptor<SignInCommand> command = ArgumentCaptor.forClass(SignInCommand.class);
        verify(userApplication).signIn(command.capture());
        assertEquals("google", command.getValue().getProvider());
        assertEquals("google-123", command.getValue().getProviderUserId());
        assertEquals("alice@example.com", command.getValue().getEmail());
        assertTrue(command.getValue().isEmailVerified());
    }

    @Test
    @DisplayName("""
            Given a provider that sends email_verified as text
            When the sign-in hook runs
            Then the email still counts as verified
            """)
    void loadUserAcceptsEmailVerifiedAsText() {
        // Given
        final OAuth2SignInService service = new OAuth2SignInService(
                request -> new DefaultOAuth2User(
                        List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                        Map.of("sub", "google-123", "email", "alice@example.com", "email_verified", "true"),
                        "sub"),
                userApplication);
        when(userApplication.signIn(any())).thenReturn(USER_ID);

        // When
        service.loadUser(request());

        // Then
        final ArgumentCaptor<SignInCommand> command = ArgumentCaptor.forClass(SignInCommand.class);
        verify(userApplication).signIn(command.capture());
        assertTrue(command.getValue().isEmailVerified());
    }

    @Test
    @DisplayName("""
            Given someone who isn't invited
            When the sign-in hook runs
            Then it fails with the not_invited code the sign-in page explains
            """)
    void loadUserFailsWhenNotInvited() {
        // Given
        when(userApplication.signIn(any())).thenThrow(new SignInNotInvitedException());

        // When
        final OAuth2AuthenticationException failure =
                assertThrows(OAuth2AuthenticationException.class, () -> signInService.loadUser(request()));

        // Then
        assertEquals("not_invited", failure.getError().getErrorCode());
        assertEquals("not_invited", SecurityConfig.signInErrorCode(failure));
    }

    @Test
    @DisplayName("""
            Given an unexpected error
            When the sign-in hook runs
            Then it still leaves as a sign-in failure, never a raw error page
            """)
    void loadUserTurnsUnexpectedErrorsIntoASignInFailure() {
        // Given
        when(userApplication.signIn(any())).thenThrow(new IllegalStateException("boom"));

        // When
        final OAuth2AuthenticationException failure =
                assertThrows(OAuth2AuthenticationException.class, () -> signInService.loadUser(request()));

        // Then
        assertEquals("sign_in_failed", failure.getError().getErrorCode());
    }

    private static OAuth2UserRequest request() {
        final ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .authorizationUri("https://example.com/authorize")
                .tokenUri("https://example.com/token")
                .build();
        final OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(), Instant.now().plusSeconds(60));
        return new OAuth2UserRequest(registration, token);
    }
}
