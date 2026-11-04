package org.manu.jobhunt.integration.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DELETE /api/jobs/{id}
 */
class DeleteJobTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a job with a round
            When its owner sends a DELETE request
            Then HTTP 204 is returned and both are gone
            """)
    void deleteJobRemovesItsRounds() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, id, RoundType.SCREENING, Instant.now());

        // When
        var response = restClient.delete().uri("/api/jobs/" + id).exchange();

        // Then
        response.expectStatus().isNoContent();
        assertEquals(0, countJobs());
        assertEquals(0, countRounds());
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user sends a DELETE request
            Then HTTP 404 is returned and the job stays
            """)
    void deleteJobReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long id = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = restClient.delete().uri("/api/jobs/" + id).exchange();

        // Then
        response.expectStatus().isNotFound();
        assertEquals(1, countJobs());
    }
}
