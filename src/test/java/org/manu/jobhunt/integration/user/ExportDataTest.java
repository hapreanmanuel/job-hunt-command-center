package org.manu.jobhunt.integration.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Instant;

/**
 * GET /api/user/export
 */
class ExportDataTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given jobs and rounds from two users, one job archived
            When one of them downloads their data
            Then it holds everything of theirs, archived included, and nothing of the other's
            """)
    void exportContainsOnlyTheCallersData() {
        // Given
        final Long job = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, job, RoundType.FINAL, Instant.now());
        final Long archived = insertJob(AUTHOR, "Archived", JobStatus.APPLIED);
        insertRound(AUTHOR, archived, RoundType.SCREENING, Instant.now());
        DATABASE.update("UPDATE app.job SET archived = true WHERE id = ?", archived);
        final Long other = insertJob(OTHER_USER, "Globex", JobStatus.INTERVIEWING);
        insertRound(OTHER_USER, other, RoundType.FINAL, Instant.now());

        // When
        var response = restClient.get().uri("/api/user/export").exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.jobs.length()").isEqualTo(2)
                .jsonPath("$.rounds.length()").isEqualTo(2);
    }
}
