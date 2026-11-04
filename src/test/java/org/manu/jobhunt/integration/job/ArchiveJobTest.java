package org.manu.jobhunt.integration.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * POST /api/jobs/{id}/archive and /unarchive
 */
class ArchiveJobTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user's job
            When they archive it and then unarchive it
            Then each returns HTTP 204 and the job follows
            """)
    void archiveAndUnarchiveJob() {
        // Given
        final Long id = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When / Then
        post(restClient, "/api/jobs/" + id + "/archive", Map.of()).expectStatus().isNoContent();
        assertEquals("true", jobColumn(id, "archived"));

        post(restClient, "/api/jobs/" + id + "/unarchive", Map.of()).expectStatus().isNoContent();
        assertEquals("false", jobColumn(id, "archived"));
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user tries to archive it
            Then HTTP 404 is returned and it isn't archived
            """)
    void archiveJobReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long id = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/archive", Map.of());

        // Then
        response.expectStatus().isNotFound();
        assertEquals("false", jobColumn(id, "archived"));
    }

    @Test
    @DisplayName("""
            Given another user's archived job
            When a user tries to unarchive it
            Then HTTP 404 is returned and it stays archived
            """)
    void unarchiveJobReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long id = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);
        DATABASE.update("UPDATE app.job SET archived = true WHERE id = ?", id);

        // When
        var response = post(restClient, "/api/jobs/" + id + "/unarchive", Map.of());

        // Then
        response.expectStatus().isNotFound();
        assertEquals("true", jobColumn(id, "archived"));
    }
}
