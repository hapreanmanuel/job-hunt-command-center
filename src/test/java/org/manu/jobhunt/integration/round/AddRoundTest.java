package org.manu.jobhunt.integration.round;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * POST /api/jobs/{jobId}/rounds
 */
class AddRoundTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a job with status applied
            When its first round is added
            Then the round is created with HTTP 201, and the job moves to interviewing by itself
            """)
    void addRoundMovesAnAppliedJobToInterviewing() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/rounds",
                Map.of("type", "SCREENING", "scheduledAt", "2026-11-05T14:00:00Z"));

        // Then
        response.expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.company").isEqualTo("Acme")
                .jsonPath("$.roleTitle").isEqualTo("Engineer")
                .jsonPath("$.type").isEqualTo("SCREENING")
                .jsonPath("$.outcome").isEqualTo("PENDING");

        assertEquals("INTERVIEWING", jobColumn(jobId, "status"));
    }

    @Test
    @DisplayName("""
            Given a job with an open offer
            When a round is added
            Then the status stays: rounds only move a job forward from the start of the process
            """)
    void addRoundKeepsALaterStatus() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.OFFER);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/rounds", Map.of("type", "FINAL"));

        // Then
        response.expectStatus().isCreated();
        assertEquals("OFFER", jobColumn(jobId, "status"));
    }

    @Test
    @DisplayName("""
            Given no round type
            When a POST request is sent
            Then HTTP 400 is returned and nothing is stored
            """)
    void addRoundReturnsBadRequestWithoutType() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/rounds", Map.of("debrief", "No type"));

        // Then
        response.expectStatus().isBadRequest();
        assertEquals(0, countRounds());
    }

    @Test
    @DisplayName("""
            Given a score outside 1 to 5
            When a POST request is sent
            Then HTTP 400 is returned
            """)
    void addRoundReturnsBadRequestForAScoreOutOfRange() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/rounds", Map.of("type", "OTHER", "feltScore", 6));

        // Then
        response.expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user adds a round to it
            Then HTTP 404 is returned and nothing is stored
            """)
    void addRoundReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/rounds", Map.of("type", "OTHER"));

        // Then
        response.expectStatus().isNotFound();
        assertEquals(0, countRounds());
    }
}
