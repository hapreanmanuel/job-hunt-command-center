package org.manu.jobhunt.integration.round;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Duration;
import java.time.Instant;

/**
 * GET /api/jobs/{jobId}/rounds
 */
class GetRoundsTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a job with rounds, one without a date
            When a GET request is sent
            Then they're returned oldest first, with the undated one last
            """)
    void getRoundsReturnsThemOldestFirst() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, jobId, RoundType.OTHER, null);
        insertRound(AUTHOR, jobId, RoundType.FINAL, Instant.now());
        insertRound(AUTHOR, jobId, RoundType.SCREENING, Instant.now().minus(Duration.ofDays(3)));

        // When
        var response = restClient.get().uri("/api/jobs/" + jobId + "/rounds").exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].type").isEqualTo("SCREENING")
                .jsonPath("$[1].type").isEqualTo("FINAL")
                .jsonPath("$[2].type").isEqualTo("OTHER");
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user sends a GET request
            Then HTTP 404 is returned
            """)
    void getRoundsReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.INTERVIEWING);

        // When
        var response = restClient.get().uri("/api/jobs/" + jobId + "/rounds").exchange();

        // Then
        response.expectStatus().isNotFound();
    }
}
