package org.manu.jobhunt.integration.round;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Instant;

/**
 * GET /api/rounds/{id}
 */
class GetRoundTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user's round
            When they send a GET request
            Then the round is returned with its job's details
            """)
    void getRoundReturnsTheRound() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        final Long id = insertRound(AUTHOR, jobId, RoundType.TECHNICAL, Instant.now());

        // When
        var response = restClient.get().uri("/api/rounds/" + id).exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.type").isEqualTo("TECHNICAL")
                .jsonPath("$.jobId").isEqualTo(jobId)
                .jsonPath("$.company").isEqualTo("Acme");
    }

    @Test
    @DisplayName("""
            Given another user's round
            When a user sends a GET request
            Then HTTP 404 is returned
            """)
    void getRoundReturnsNotFoundForAnotherUsersRound() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.INTERVIEWING);
        final Long id = insertRound(OTHER_USER, jobId, RoundType.TECHNICAL, Instant.now());

        // When
        var response = restClient.get().uri("/api/rounds/" + id).exchange();

        // Then
        response.expectStatus().isNotFound();
    }
}
