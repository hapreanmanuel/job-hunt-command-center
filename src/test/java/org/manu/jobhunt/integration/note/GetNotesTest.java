package org.manu.jobhunt.integration.note;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

/**
 * GET /api/jobs/{jobId}/notes
 */
class GetNotesTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a job with two notes
            When a GET request is sent
            Then they're returned newest first
            """)
    void getNotesReturnsThemNewestFirst() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);
        final Long older = insertNote(AUTHOR, jobId, "First");
        DATABASE.update("UPDATE app.note SET created_at = now() - interval '1 day' WHERE id = ?", older);
        insertNote(AUTHOR, jobId, "Second");

        // When
        var response = restClient.get().uri("/api/jobs/" + jobId + "/notes").exchange();

        // Then
        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].text").isEqualTo("Second")
                .jsonPath("$[1].text").isEqualTo("First");
    }

    @Test
    @DisplayName("""
            Given another user's job with a note
            When a user asks for its notes
            Then HTTP 404 is returned
            """)
    void getNotesReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);
        insertNote(OTHER_USER, jobId, "Private");

        // When
        var response = restClient.get().uri("/api/jobs/" + jobId + "/notes").exchange();

        // Then
        response.expectStatus().isNotFound();
    }
}
