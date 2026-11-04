package org.manu.jobhunt.integration.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

/**
 * GET /api/user
 */
class GetCurrentUserTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a signed-in user
            When a GET request is sent
            Then their id is returned
            """)
    void getCurrentUserReturnsTheirId() {
        // When
        var response = restClient.get().uri("/api/user").exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(AUTHOR.toString());
    }
}
