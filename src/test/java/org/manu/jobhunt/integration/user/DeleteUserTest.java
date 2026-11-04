package org.manu.jobhunt.integration.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.integration.AbstractIntegrationTest;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DELETE /api/user
 */
class DeleteUserTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given a user with a job and a round, signed in on two browsers
            When they delete their data
            Then HTTP 204 is returned, nothing of theirs is left and both browsers are signed out
            """)
    void deleteUserRemovesEverythingOfTheirs() {
        // Given
        final Long job = insertJob(AUTHOR, "Acme", JobStatus.INTERVIEWING);
        insertRound(AUTHOR, job, RoundType.FINAL, Instant.now());
        final RestTestClient secondBrowser = clientSignedInAs(AUTHOR);

        // When
        var response = restClient.delete().uri("/api/user").exchange();

        // Then
        response.expectStatus().isNoContent();
        assertEquals(0, countJobs());
        assertEquals(0, countRounds());
        assertEquals(0, count("app.app_user WHERE id = '%s'".formatted(AUTHOR)));
        assertEquals(0, count("app.spring_session WHERE principal_name = '%s'".formatted(AUTHOR)));
        restClient.get().uri("/api/jobs").exchange().expectStatus().isUnauthorized();
        secondBrowser.get().uri("/api/jobs").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("""
            Given two users with data
            When one of them deletes theirs
            Then the other's data and session are untouched
            """)
    void deleteUserLeavesOtherUsersAlone() {
        // Given
        insertJob(AUTHOR, "Acme", JobStatus.APPLIED);
        final Long other = insertJob(OTHER_USER, "Globex", JobStatus.INTERVIEWING);
        insertRound(OTHER_USER, other, RoundType.FINAL, Instant.now());

        // When
        restClient.delete().uri("/api/user").exchange().expectStatus().isNoContent();

        // Then
        assertEquals(1, countJobs());
        assertEquals(1, countRounds());
        otherClient.get().uri("/api/jobs").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[0].company").isEqualTo("Globex");
    }

    private int count(String tableAndCondition) {
        return DATABASE.queryForObject("SELECT count(*) FROM " + tableAndCondition, Integer.class);
    }
}
