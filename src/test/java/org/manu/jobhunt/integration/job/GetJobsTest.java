package org.manu.jobhunt.integration.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

/**
 * GET /api/jobs
 */
class GetJobsTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given jobs from two users
            When one of them sends a GET request
            Then they only see their own
            """)
    void getJobsReturnsOnlyTheCallersJobs() {
        // Given
        insertJob(AUTHOR, "Acme", JobStatus.APPLIED);
        insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = restClient.get().uri("/api/jobs").exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].company").isEqualTo("Acme");
    }

    @Test
    @DisplayName("""
            Given an archived job
            When a GET request is sent
            Then it's hidden unless archived jobs are asked for
            """)
    void getJobsHidesArchivedJobsUnlessAsked() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);
        DATABASE.update("UPDATE app.job SET archived = true WHERE id = ?", id);

        // When / Then
        restClient.get().uri("/api/jobs").exchange()
                .expectBody().jsonPath("$.length()").isEqualTo(0);
        restClient.get().uri("/api/jobs?includeArchived=true").exchange()
                .expectBody().jsonPath("$.length()").isEqualTo(1);
    }
}
