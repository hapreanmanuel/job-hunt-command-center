package org.manu.jobhunt.integration.overview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Duration;
import java.time.Instant;

/**
 * GET /api/jobs/{id}/situation
 */
class GetSituationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a job with a past round and no debrief
            When its situation is requested
            Then it needs a debrief, and the round is named
            """)
    void getSituationReturnsTheJobsSituation() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, jobId, RoundType.SCREENING, Instant.now().minus(Duration.ofDays(1)));

        // When
        var response = restClient.get().uri("/api/jobs/" + jobId + "/situation").exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.situation").isEqualTo("NEEDS_DEBRIEF")
                .jsonPath("$.round.type").isEqualTo("SCREENING");
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user requests its situation
            Then HTTP 404 is returned
            """)
    void getSituationReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = restClient.get().uri("/api/jobs/" + jobId + "/situation").exchange();

        // Then
        response.expectStatus().isNotFound();
    }
}
