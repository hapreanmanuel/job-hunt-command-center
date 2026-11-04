package org.manu.jobhunt.integration.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

/**
 * GET /api/jobs/{id}
 */
class GetJobTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user's job
            When they send a GET request
            Then the job is returned with its color
            """)
    void getJobReturnsTheJob() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = restClient.get().uri("/api/jobs/" + id).exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.company").isEqualTo("Acme")
                .jsonPath("$.colorIndex").isEqualTo(id % 12);
    }

    @Test
    @DisplayName("""
            Given no such job
            When a GET request is sent
            Then HTTP 404 is returned
            """)
    void getJobReturnsNotFoundWhenItDoesNotExist() {
        // When
        var response = restClient.get().uri("/api/jobs/999").exchange();

        // Then
        response.expectStatus().isNotFound();
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user sends a GET request
            Then HTTP 404 is returned, so its existence isn't even revealed
            """)
    void getJobReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long id = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = restClient.get().uri("/api/jobs/" + id).exchange();

        // Then
        response.expectStatus().isNotFound();
    }
}
