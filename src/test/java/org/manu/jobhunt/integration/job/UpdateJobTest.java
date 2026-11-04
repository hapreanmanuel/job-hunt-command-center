package org.manu.jobhunt.integration.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PUT /api/jobs/{id}
 */
class UpdateJobTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user's job
            When they send a PUT request
            Then the job is updated
            """)
    void updateJobChangesTheJob() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = put(restClient, "/api/jobs/" + id, Map.of(
                "company", "Acme Corp", "roleTitle", "Staff Engineer", "status", "APPLIED", "location", "Cluj-Napoca"));

        // Then
        response.expectStatus().isOk().expectBody().jsonPath("$.company").isEqualTo("Acme Corp");

        assertEquals("Staff Engineer", jobColumn(id, "role_title"));
        assertEquals("Cluj-Napoca", jobColumn(id, "location"));
    }

    @Test
    @DisplayName("""
            Given a closed job
            When a PUT request leaves the status out, or sends a different one
            Then its status and closed date stay: only the status endpoint changes them
            """)
    void updateJobLeavesTheStatusAlone() {
        // Given
        final LocalDate closed = LocalDate.now().minusDays(3);
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.REJECTED);
        DATABASE.update("UPDATE app.job SET closed_date = ? WHERE id = ?", closed, id);

        // When
        put(restClient, "/api/jobs/" + id, Map.of("company", "Acme", "roleTitle", "Engineer"))
                .expectStatus().isOk();
        put(restClient, "/api/jobs/" + id, Map.of("company", "Acme", "roleTitle", "Engineer", "status", "OFFER"))
                .expectStatus().isOk();

        // Then
        assertEquals("REJECTED", jobColumn(id, "status"));
        assertEquals(closed.toString(), jobColumn(id, "closed_date"));
    }

    @Test
    @DisplayName("""
            Given a blank role
            When a PUT request is sent
            Then HTTP 400 is returned and the job is unchanged
            """)
    void updateJobReturnsBadRequestForABlankRole() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = put(restClient, "/api/jobs/" + id, Map.of("company", "Acme", "roleTitle", " "));

        // Then
        response.expectStatus().isBadRequest();
        assertEquals("Engineer", jobColumn(id, "role_title"));
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user sends a PUT request
            Then HTTP 404 is returned and the job is unchanged
            """)
    void updateJobReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long id = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = put(restClient, "/api/jobs/" + id, Map.of("company", "Hacked", "roleTitle", "Engineer"));

        // Then
        response.expectStatus().isNotFound();
        assertEquals("Globex", jobColumn(id, "company"));
    }
}
