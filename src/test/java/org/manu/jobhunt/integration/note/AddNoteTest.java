package org.manu.jobhunt.integration.note;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * POST /api/jobs/{jobId}/notes
 */
class AddNoteTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user's job
            When they add a note
            Then it's created with HTTP 201, dated now, and belongs to them
            """)
    void addNoteCreatesADatedNote() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/notes", Map.of("text", "Only posted on LinkedIn"));

        // Then
        response.expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.jobId").isEqualTo(jobId)
                .jsonPath("$.text").isEqualTo("Only posted on LinkedIn")
                .jsonPath("$.createdAt").exists();

        assertEquals(AUTHOR.toString(), DATABASE.queryForObject("SELECT owner_id::text FROM app.note", String.class));
    }

    @Test
    @DisplayName("""
            Given an empty note
            When a POST request is sent
            Then HTTP 400 is returned and nothing is stored
            """)
    void addNoteReturnsBadRequestWhenBlank() {
        // Given
        final Long jobId = insertJob(AUTHOR, "Acme", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/notes", Map.of("text", "   "));

        // Then
        response.expectStatus().isBadRequest();
        assertEquals(0, countNotes());
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a user adds a note to it
            Then HTTP 404 is returned and nothing is stored
            """)
    void addNoteReturnsNotFoundForAnotherUsersJob() {
        // Given
        final Long jobId = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When
        var response = post(restClient, "/api/jobs/" + jobId + "/notes", Map.of("text", "Mine now"));

        // Then
        response.expectStatus().isNotFound();
        assertEquals(0, countNotes());
    }
}
