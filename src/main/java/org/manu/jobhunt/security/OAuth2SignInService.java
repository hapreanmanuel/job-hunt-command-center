package org.manu.jobhunt.security;

import lombok.extern.slf4j.Slf4j;
import org.manu.jobhunt.application.user.UserApplication;
import org.manu.jobhunt.application.user.command.SignInCommand;
import org.manu.jobhunt.application.user.exception.SignInNotInvitedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * The hook Spring Security calls after the provider confirms who signed in. The returned SignedInUser
 * is what the session keeps.
 * <p>
 * Every failure leaves as an OAuth2AuthenticationException: it's the only kind Spring Security
 * sends to the failure handler; anything else would end on a raw error page.
 */
@Slf4j
@Service
@Profile("!local")
public class OAuth2SignInService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /** Asks the provider who signed in. */
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> providerUsers;
    private final UserApplication userApplication;

    @Autowired
    public OAuth2SignInService(UserApplication userApplication) {
        this(new DefaultOAuth2UserService(), userApplication);
    }

    OAuth2SignInService(
            OAuth2UserService<OAuth2UserRequest, OAuth2User> providerUsers,
            UserApplication userApplication) {
        this.providerUsers = providerUsers;
        this.userApplication = userApplication;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        final OAuth2User providerUser = providerUsers.loadUser(request);
        // Compared as text: depending on the endpoint, providers send true or "true".
        final Object emailVerified = providerUser.getAttribute("email_verified");
        final SignInCommand command = SignInCommand.builder()
                .provider(request.getClientRegistration().getRegistrationId())
                .providerUserId(providerUser.getName())
                .username(providerUser.getAttribute("login"))
                .email(providerUser.getAttribute("email"))
                .emailVerified("true".equals(String.valueOf(emailVerified)))
                .build();

        try {
            return new SignedInUser(userApplication.signIn(command));
        } catch (SignInNotInvitedException exception) {
            throw new OAuth2AuthenticationException(new OAuth2Error("not_invited"), exception.getMessage());
        } catch (RuntimeException exception) {
            log.error("Sign-in failed after the provider confirmed the user", exception);
            throw new OAuth2AuthenticationException(new OAuth2Error("sign_in_failed"), "Sign-in failed", exception);
        }
    }
}
