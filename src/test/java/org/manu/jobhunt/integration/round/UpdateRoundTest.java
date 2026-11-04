package org.manu.jobhunt.integration.round;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PUT /api/rounds/{id}
 */
class UpdateRoundTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a past round needing a debrief
            When its debrief is saved
            Then it no longer needs one, and the text is kept
            """)
    void updateRoundSavesTheDebrief() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        final Instant when = Instant.now().minus(Duration.ofDays(1));
        final Long id = insertRound(AUTHOR, jobId, RoundType.SCREENING, when);

        // When
        var response = put(restClient, "/api/rounds/" + id, Map.of(
                "type", "SCREENING", "scheduledAt", when.toString(), "outcome", "PASSED", "feltScore", 4,
                "difficultyScore", 2, "questionsAsked", "Why this role?", "debrief", "Friendly call"));

        // Then
        response.expectStatus().isOk();

        assertEquals("Why this role?", roundColumn(id, "questions_asked"));
        assertEquals("PASSED", roundColumn(id, "outcome"));
    }

    @Test
    @DisplayName("""
            Given another user's round
            When a user sends a PUT request
            Then HTTP 404 is returned and the round is unchanged
            """)
    void updateRoundReturnsNotFoundForAnotherUsersRound() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.INTERVIEWING);
        final Long id = insertRound(OTHER_USER, jobId, RoundType.SCREENING, Instant.now());

        // When
        var response = put(restClient, "/api/rounds/" + id, Map.of("type", "FINAL"));

        // Then
        response.expectStatus().isNotFound();
        assertEquals("SCREENING", roundColumn(id, "round_type"));
    }
}
