package org.manu.jobhunt.integration.note;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DELETE /api/notes/{id}
 */
class DeleteNoteTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user's note
            When they send a DELETE request
            Then HTTP 204 is returned and the note is gone, while the job stays
            """)
    void deleteNoteRemovesTheNote() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);
        final Long id = insertNote(AUTHOR, jobId, "Old news");

        // When
        var response = restClient.delete().uri("/api/notes/" + id).exchange();

        // Then
        response.expectStatus().isNoContent();
        assertEquals(0, countNotes());
        assertEquals(1, countJobs());
    }

    @Test
    @DisplayName("""
            Given another user's note
            When a user sends a DELETE request
            Then HTTP 404 is returned and the note stays
            """)
    void deleteNoteReturnsNotFoundForAnotherUsersNote() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);
        final Long id = insertNote(OTHER_USER, jobId, "Private");

        // When
        var response = restClient.delete().uri("/api/notes/" + id).exchange();

        // Then
        response.expectStatus().isNotFound();
        assertEquals(1, countNotes());
    }
}
