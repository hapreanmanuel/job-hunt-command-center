package org.manu.jobhunt.integration.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.integration.AbstractDatabaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

/**
 * A local run: one fixed user and no sign-in, which takes the explicit "local" profile.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("local")
class LocalProfileTest extends AbstractDatabaseTest {

    private static final UUID LOCAL_USER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private RestTestClient restClient;

    @BeforeEach
    void keepTheLocalUser() {
        DATABASE.update("INSERT INTO app.app_user (id) VALUES (?) ON CONFLICT DO NOTHING", LOCAL_USER);
    }

    @Test
    @DisplayName("""
            Given the local profile
            When a page and the API are called without signing in
            Then both answer, as the fixed user
            """)
    void noSignInIsNeeded() {
        // When / Then
        restClient.get().uri("/").exchange().expectStatus().isOk();
        restClient.get().uri("/api/user").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(LOCAL_USER.toString());
    }

    @Test
    @DisplayName("""
            Given the local profile
            When the sign-in page is opened
            Then it sends you to Home, as there's nothing to sign in to
            """)
    void theSignInPageGoesHome() {
        // When / Then
        restClient.get().uri("/login").exchange().expectStatus().is3xxRedirection();
    }
}
