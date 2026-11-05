package org.manu.jobhunt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.savedrequest.NullRequestCache;

import java.io.IOException;

/**
 * Who may call what, and how signing in works.
 * <ul>
 *     <li>Hosted (the default): sign in with a provider. GET /oauth2/authorization/{provider} starts it,
 *     the provider calls back /login/oauth2/code/{provider}, and the session lives in an HttpOnly
 *     cookie.</li>
 *     <li>Local (profile "local"): one fixed user, no sign-in. It takes an explicit profile, so
 *     a missing setting can't leave a hosted copy open.</li>
 *     <li>CSRF protection is on in both; pages get their token from GET /api/csrf.</li>
 *     <li>API calls get a 401 JSON body, pages a redirect to /login.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private static final String CONTENT_SECURITY_POLICY = "default-src 'self'; script-src 'self'; style-src 'self'; "
            + "img-src 'self' data:; frame-ancestors 'none'; base-uri 'self'; form-action 'self'";

    @Bean
    @Profile("!local")
    SecurityFilterChain hostedFilterChain(
            HttpSecurity http,
            OAuth2SignInService signInService,
            ClientRegistrationRepository clientRegistrations) throws Exception {

        withSecurityHeaders(http)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/login", "/privacy", "/terms", "/css/**", "/js/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(login -> login
                        .loginPage("/login")
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(withAccountPicker(clientRegistrations)))
                        .userInfoEndpoint(userInfo -> userInfo.userService(signInService))
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) ->
                                response.sendRedirect("/login?error=" + signInErrorCode(exception))))
                // No "request cache": a refused request would otherwise be saved in a new session,
                // so every bot hit would add a session row. Sign-in always returns to Home.
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .logout(logout -> logout
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> refuse(request, response))
                        .accessDeniedHandler((request, response, exception) ->
                                writeMessage(response, HttpStatus.FORBIDDEN, "Access denied")));

        return http.build();
    }

    @Bean
    @Profile("local")
    SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
        withSecurityHeaders(http).authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
        return http.build();
    }

    private static HttpSecurity withSecurityHeaders(HttpSecurity http) throws Exception {
        return http.headers(headers -> headers
                .contentSecurityPolicy(policy -> policy.policyDirectives(CONTENT_SECURITY_POLICY))
                .referrerPolicy(referrer -> referrer.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
    }

    /** Asks the provider to show its account picker, so someone with two accounts can choose. */
    private static OAuth2AuthorizationRequestResolver withAccountPicker(ClientRegistrationRepository clientRegistrations) {
        final DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrations, OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        resolver.setAuthorizationRequestCustomizer(request ->
                request.additionalParameters(parameters -> parameters.put("prompt", "select_account")));
        return resolver;
    }

    /** Why a sign-in failed, for the sign-in page. Only plain codes go into the address. */
    static String signInErrorCode(AuthenticationException exception) {
        final String code = exception instanceof OAuth2AuthenticationException oauth2
                ? oauth2.getError().getErrorCode()
                : null;
        return code != null && code.matches("[a-z_]{1,40}") ? code : "sign_in_failed";
    }

    private static void refuse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            writeMessage(response, HttpStatus.UNAUTHORIZED, "Sign in required");
        } else {
            response.sendRedirect("/login");
        }
    }

    /** Same {"message": "..."} body as GlobalExceptionHandler, for errors raised before a controller runs. */
    private static void writeMessage(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"%s\"}".formatted(message));
    }
}
