package org.manu.jobhunt.integration.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.integration.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Who may reach what, across the whole app.
 */
class SecurityRulesTest extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    @DisplayName("""
            Given every API route the application has, including any added later
            When a visitor calls it
            Then HTTP 401 is returned
            """)
    void everyApiRouteRequiresSignIn() {
        // Given
        final List<String> checked = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mappings.getHandlerMethods().entrySet()) {
            for (String pattern : entry.getKey().getPatternValues()) {
                if (pattern.startsWith("/api/")) {
                    final String path = pattern.replaceAll("\\{[^}]+}", "1");
                    for (var method : entry.getKey().getMethodsCondition().getMethods()) {
                        // When
                        final RestTestClient.ResponseSpec response = clientWithoutSession()
                                .method(HttpMethod.valueOf(method.name()))
                                .uri(path)
                                .exchange();

                        // Then: writes are stopped by CSRF first, which is also a refusal.
                        if (method.name().equals("GET")) {
                            response.expectStatus().isUnauthorized();
                        } else {
                            response.expectStatus().isForbidden();
                        }
                        checked.add(method + " " + path);
                    }
                }
            }
        }
        assertTrue(checked.size() > 15, checked.toString());
    }

    @Test
    @DisplayName("""
            Given a visitor
            When they open Home or a job
            Then they're sent to the sign-in page
            """)
    void pagesRedirectVisitorsToSignIn() {
        // When / Then
        clientWithoutSession().get().uri("/").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value(HttpHeaders.LOCATION, location -> assertTrue(location.endsWith("/login")));
        clientWithoutSession().get().uri("/jobs/1").exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    @DisplayName("""
            Given a visitor
            When they open the sign-in page or the health check
            Then both answer, and the page offers Google
            """)
    void signInPageAndHealthArePublic() {
        // When / Then
        clientWithoutSession().get().uri("/login").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(page -> assertTrue(page.contains("/oauth2/authorization/google")));
        clientWithoutSession().get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("""
            Given a visitor
            When they open the Privacy and Terms pages
            Then both answer without signing in, and say who to contact
            """)
    void privacyAndTermsArePublic() {
        // When / Then
        for (String page : List.of("/privacy", "/terms")) {
            clientWithoutSession().get().uri(page).exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(body -> assertTrue(body.contains("mailto:contact@example.com"), page));
        }
    }

    @Test
    @DisplayName("""
            Given a visitor
            When they open the sign-in page
            Then it warns about free hosting, and links to the access request, Terms and Privacy
            """)
    void signInPageStatesTheConditions() {
        // When
        var response = clientWithoutSession().get().uri("/login").exchange();

        // Then
        response.expectBody(String.class).value(page -> {
            assertTrue(page.contains("no backups"), page);
            assertTrue(page.contains("https://example.com/request-access"), page);
            assertTrue(page.contains("href=\"/terms\""), page);
            assertTrue(page.contains("href=\"/privacy\""), page);
        });
    }

    @Test
    @DisplayName("""
            Given a visitor
            When they start signing in
            Then they're sent to Google, with the account picker
            """)
    void signInStartsAtGoogle() {
        // When
        var response = clientWithoutSession().get().uri("/oauth2/authorization/google").exchange();

        // Then
        response.expectStatus().is3xxRedirection()
                .expectHeader().value(HttpHeaders.LOCATION, location -> {
                    assertTrue(location.startsWith("https://accounts.google.com/"), location);
                    assertTrue(location.contains("prompt=select_account"), location);
                });
    }

    @Test
    @DisplayName("""
            Given a refused sign-in
            When the sign-in page shows the reason
            Then it says the account hasn't been invited
            """)
    void signInPageExplainsARefusal() {
        // When
        var response = clientWithoutSession().get().uri("/login?error=not_invited").exchange();

        // Then
        response.expectBody(String.class)
                .value(page -> assertTrue(page.contains("been invited yet"), page));
    }

    @Test
    @DisplayName("""
            Given a signed-in user's session, but no CSRF token
            When a write is sent
            Then HTTP 403 is returned and nothing is stored: another site can't write as them
            """)
    void writesRequireTheCsrfToken() {
        // Given
        final String cookie = sessionCookieOf(restClient);

        // When
        var response = clientWithoutSession().post().uri("/api/jobs")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("company", "Acme", "roleTitle", "Engineer"))
                .exchange();

        // Then
        response.expectStatus().isForbidden();
        assertEquals(0, countJobs());
    }

    @Test
    @DisplayName("""
            Given any page
            When it's served
            Then it carries the security headers, with scripts from this site only
            """)
    void pagesCarrySecurityHeaders() {
        // When
        var response = clientWithoutSession().get().uri("/login").exchange();

        // Then
        response.expectHeader().value("Content-Security-Policy", policy -> {
                    assertTrue(policy.contains("script-src 'self'"), policy);
                    assertTrue(policy.contains("frame-ancestors 'none'"), policy);
                })
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff");
    }

    /** The cookie a signed-in client sends, taken from a request it made. */
    private String sessionCookieOf(RestTestClient client) {
        return client.get().uri("/api/user").exchange()
                .returnResult(String.class)
                .getRequestHeaders()
                .getFirst(HttpHeaders.COOKIE);
    }
}
