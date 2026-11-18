package org.manu.jobhunt.integration.journey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The core loop end to end: add a job, schedule a round, debrief it.
 */
class TrackAnInterviewJourneyTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a new job
            When a round is scheduled, passes its date and is debriefed
            Then the job's situation follows each step
            """)
    void aJobMovesThroughTheLoop() {
        // Add a job
        final Long jobId = postForId("/api/jobs", Map.of("company", "Acme", "roleTitle", "Engineer"));
        assertSituation(jobId, "WAITING");

        // Schedule a round for tomorrow
        final Instant tomorrow = Instant.now().plus(Duration.ofDays(1));
        final Long roundId = postForId("/api/jobs/" + jobId + "/rounds",
                Map.of("type", "SCREENING", "scheduledAt", tomorrow.toString()));
        assertEquals("INTERVIEWING", jobColumn(jobId, "status"));
        assertSituation(jobId, "ROUND_SOON");

        // The round happens
        final Instant yesterday = Instant.now().minus(Duration.ofDays(1));
        put(restClient, "/api/rounds/" + roundId, Map.of("type", "SCREENING", "scheduledAt", yesterday.toString()))
                .expectStatus().isOk();
        assertSituation(jobId, "NEEDS_DEBRIEF");

        // Debrief it
        put(restClient, "/api/rounds/" + roundId, Map.of(
                "type", "SCREENING", "scheduledAt", yesterday.toString(), "outcome", "PASSED",
                "feltScore", 4, "difficultyScore", 2))
                .expectStatus().isOk();
        assertSituation(jobId, "PASSED_NO_NEXT");
    }

    private void assertSituation(Long jobId, String expected) {
        restClient.get().uri("/api/jobs/" + jobId + "/situation").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.situation").isEqualTo(expected);
    }
}
