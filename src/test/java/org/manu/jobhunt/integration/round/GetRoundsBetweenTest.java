package org.manu.jobhunt.integration.round;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Instant;

/**
 * GET /api/rounds?from=...&to=...
 */
class GetRoundsBetweenTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a late-evening round in UTC
            When the calendar asks for its week
            Then the round is returned, with its job's details, and another month is empty
            """)
    void getRoundsBetweenReturnsRoundsInTheRange() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, jobId, RoundType.FINAL, Instant.parse("2026-11-08T23:30:00Z"));

        // When / Then
        restClient.get().uri("/api/rounds?from=2026-11-02&to=2026-11-08").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].company").isEqualTo("Acme");
        restClient.get().uri("/api/rounds?from=2026-12-01&to=2026-12-07").exchange()
                .expectBody().jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("""
            Given a round just outside the range in UTC
            When the calendar asks for the range
            Then it's still returned: only the browser knows the viewer's timezone
            """)
    void getRoundsBetweenPadsTheRange() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, jobId, RoundType.FINAL, Instant.parse("2026-11-09T01:00:00Z"));

        // When
        var response = restClient.get().uri("/api/rounds?from=2026-11-02&to=2026-11-08").exchange();

        // Then
        response.expectBody().jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    @DisplayName("""
            Given rounds from two users in the same week
            When one of them asks for it
            Then they only see their own
            """)
    void getRoundsBetweenReturnsOnlyTheCallersRounds() {
        // Given
        final Instant when = Instant.parse("2026-11-05T14:00:00Z");
        insertRound(AUTHOR, insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING), RoundType.FINAL, when);
        insertRound(OTHER_USER, insertJob(OTHER_USER, "Globex", JobStatus.INTERVIEWING), RoundType.FINAL, when);

        // When
        var response = restClient.get().uri("/api/rounds?from=2026-11-02&to=2026-11-08").exchange();

        // Then
        response.expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].company").isEqualTo("Acme");
    }

    @Test
    @DisplayName("""
            Given a missing or malformed date
            When a GET request is sent
            Then HTTP 400 is returned
            """)
    void getRoundsBetweenReturnsBadRequestForBadDates() {
        // When / Then
        restClient.get().uri("/api/rounds?from=2026-11-02").exchange().expectStatus().isBadRequest();
        restClient.get().uri("/api/rounds?from=soon&to=later").exchange().expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("""
            Given a round of an archived job
            When the calendar asks for that week
            Then it isn't there
            """)
    void getRoundsBetweenLeavesOutArchivedJobs() {
        // Given
        final Long job = insertJob(AUTHOR, "Archived", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, job, RoundType.FINAL, Instant.parse("2026-11-05T14:00:00Z"));
        DATABASE.update("UPDATE app.job SET archived = true WHERE id = ?", job);

        // When
        var response = restClient.get().uri("/api/rounds?from=2026-11-02&to=2026-11-08").exchange();

        // Then
        response.expectBody().jsonPath("$.length()").isEqualTo(0);
    }
}
