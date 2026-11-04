package org.manu.jobhunt.integration.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * POST /api/jobs/{id}/status
 */
class ChangeJobStatusTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a job moved to a closed status with a date
            When the POST request is sent
            Then that date is recorded as the closed date
            """)
    void changeStatusRecordsTheClosedDate() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/status", Map.of("status", "GHOSTED", "date", "2026-10-01"));

        // Then
        response.expectStatus().isOk().expectBody().jsonPath("$.closedDate").isEqualTo("2026-10-01");
    }

    @Test
    @DisplayName("""
            Given a closed job
            When it's reopened
            Then the closed date is cleared
            """)
    void changeStatusClearsTheClosedDateWhenReopening() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.REJECTED);
        DATABASE.update("UPDATE app.job SET closed_date = '2026-10-01' WHERE id = ?", id);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/status", Map.of("status", "INTERVIEWING"));

        // Then
        response.expectStatus().isOk();
        assertNull(jobColumn(id, "closed_date"));
    }

    @Test
    @DisplayName("""
            Given a wishlist job
            When it moves to applied without a date
            Then the applied date defaults to today
            """)
    void changeStatusDefaultsTheAppliedDateToToday() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.WISHLIST);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/status", Map.of("status", "APPLIED"));

        // Then
        response.expectStatus().isOk();
        assertEquals(LocalDate.now().toString(), jobColumn(id, "applied_date"));
    }

    @Test
    @DisplayName("""
            Given an applied job
            When it moves back to the wishlist
            Then the applied date is cleared
            """)
    void changeStatusClearsTheAppliedDateOnTheWishlist() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/status", Map.of("status", "WISHLIST"));

        // Then
        response.expectStatus().isOk();
        assertNull(jobColumn(id, "applied_date"));
    }

    @Test
    @DisplayName("""
            Given no status
            When a POST request is sent
            Then HTTP 400 is returned
            """)
    void changeStatusReturnsBadRequestWithoutStatus() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/status", Map.of("date", "2026-10-01"));

        // Then
        response.expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user sends a POST request
            Then HTTP 404 is returned and the status is unchanged
            """)
    void changeStatusReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long id = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/status", Map.of("status", "REJECTED"));

        // Then
        response.expectStatus().isNotFound();
        assertEquals("APPLIED", jobColumn(id, "status"));
    }
}
