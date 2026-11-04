package org.manu.jobhunt.integration.overview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

/**
 * GET /api/overview
 */
class GetOverviewTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given jobs in different situations
            When Home loads
            Then the most urgent come first, and closed jobs are listed apart
            """)
    void getOverviewSortsByUrgencyAndSeparatesClosedJobs() {
        // Given
        final Long waiting = insertJob(AUTHOR, "Waiting", JobStatus.APPLIED);
        final Long wishlist = insertJob(AUTHOR, "Wishlist", JobStatus.WISHLIST);
        final Long offer = insertJob(AUTHOR, "Offer", JobStatus.OFFER);
        final Long soon = insertJob(AUTHOR, "Soon", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, soon, RoundType.TECHNICAL, Instant.now().plus(Duration.ofHours(5)));
        insertJob(AUTHOR, "Closed", JobStatus.REJECTED);

        // When
        var response = restClient.get().uri("/api/overview?tz=Europe/Bucharest").exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.active.length()").isEqualTo(4)
                .jsonPath("$.active[0].job.id").isEqualTo(soon)
                .jsonPath("$.active[0].situation.situation").isEqualTo("ROUND_SOON")
                .jsonPath("$.active[0].situation.round.type").isEqualTo("TECHNICAL")
                .jsonPath("$.active[1].job.id").isEqualTo(offer)
                .jsonPath("$.active[2].job.id").isEqualTo(wishlist)
                .jsonPath("$.active[3].job.id").isEqualTo(waiting)
                .jsonPath("$.closed.length()").isEqualTo(1)
                .jsonPath("$.closed[0].company").isEqualTo("Closed");
    }

    @Test
    @DisplayName("""
            Given an application from today and one from a month ago, and a round held earlier
            When Home loads
            Then only this week's application is counted
            """)
    void getOverviewCountsThisWeek() {
        // Given
        insertJob(AUTHOR, "Recent", JobStatus.APPLIED);
        insertJob(AUTHOR, "Old", JobStatus.APPLIED, LocalDate.now().minusDays(30));

        // When
        var response = restClient.get().uri("/api/overview").exchange();

        // Then
        response.expectBody()
                .jsonPath("$.applicationsSentThisWeek").isEqualTo(1)
                .jsonPath("$.roundsHeldThisWeek").isEqualTo(0);
    }

    @Test
    @DisplayName("""
            Given jobs from two users
            When one of them loads Home
            Then they only see their own
            """)
    void getOverviewReturnsOnlyTheCallersJobs() {
        // Given
        insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);
        insertJob(OTHER_USER, "Initech", JobStatus.REJECTED);

        // When
        var response = restClient.get().uri("/api/overview").exchange();

        // Then
        response.expectBody()
                .jsonPath("$.active.length()").isEqualTo(0)
                .jsonPath("$.closed.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("""
            Given an unknown timezone
            When a GET request is sent
            Then HTTP 400 is returned
            """)
    void getOverviewReturnsBadRequestForAnUnknownTimezone() {
        // When
        var response = restClient.get().uri("/api/overview?tz=Mars/Olympus").exchange();

        // Then
        response.expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("""
            Given rounds held today, one of them on an archived job
            When Home loads
            Then only the other one is counted
            """)
    void getOverviewLeavesOutArchivedJobs() {
        // Given
        final Instant justNow = Instant.now().minusSeconds(60);
        insertRound(AUTHOR, insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING),
                RoundType.FINAL, justNow);
        final Long archived = insertJob(AUTHOR, "Archived", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, archived, RoundType.FINAL, justNow);
        DATABASE.update("UPDATE app.job SET archived = true WHERE id = ?", archived);

        // When
        var response = restClient.get().uri("/api/overview").exchange();

        // Then
        response.expectBody().jsonPath("$.roundsHeldThisWeek").isEqualTo(1);
    }
}
