package org.manu.jobhunt.integration.round;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DELETE /api/rounds/{id}
 */
class DeleteRoundTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user's round
            When they send a DELETE request
            Then HTTP 204 is returned and the round is gone, while the job stays
            """)
    void deleteRoundRemovesTheRound() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        final Long id = insertRound(AUTHOR, jobId, RoundType.SCREENING, Instant.now());

        // When
        var response = restClient.delete().uri("/api/rounds/" + id).exchange();

        // Then
        response.expectStatus().isNoContent();
        assertEquals(0, countRounds());
        assertEquals(1, countJobs());
    }

    @Test
    @DisplayName("""
            Given another user's round
            When a user sends a DELETE request
            Then HTTP 404 is returned and the round stays
            """)
    void deleteRoundReturnsNotFoundForAnotherUsersRound() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.INTERVIEWING);
        final Long id = insertRound(OTHER_USER, jobId, RoundType.SCREENING, Instant.now());

        // When
        var response = restClient.delete().uri("/api/rounds/" + id).exchange();

        // Then
        response.expectStatus().isNotFound();
        assertEquals(1, countRounds());
    }
}
