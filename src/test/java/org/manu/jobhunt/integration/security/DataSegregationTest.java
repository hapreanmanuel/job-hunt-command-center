package org.manu.jobhunt.integration.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.integration.AbstractIntegrationTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What the schema guarantees about ownership. That each endpoint only reaches the caller's data
 * is covered by that endpoint's own test.
 */
class DataSegregationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("""
            Given every table in the schema, including any added later
            When its columns are checked
            Then each one has an owner: the build fails if a new table doesn't
            """)
    void everyTableOfUserDataHasAnOwner() {
        // When
        final List<String> withoutOwner = DATABASE.queryForList(
                """
                        SELECT t.table_name
                        FROM information_schema.tables t
                        WHERE t.table_schema = 'app'
                          AND t.table_name NOT IN ('app_user', 'flyway_schema_history',
                                                   'spring_session', 'spring_session_attributes')
                          AND NOT EXISTS (SELECT 1 FROM information_schema.columns c
                                          WHERE c.table_schema = 'app' AND c.table_name = t.table_name
                                            AND c.column_name = 'owner_id' AND c.is_nullable = 'NO')
                        """,
                String.class);

        // Then
        assertEquals(List.of(), withoutOwner);
    }

    @Test
    @DisplayName("""
            Given another user's job
            When a round is attached to it
            Then the database refuses, through the composite foreign key
            """)
    void aRoundCannotBeAttachedToAnotherUsersJob() {
        // Given
        final Long job = insertJob(OTHER_USER, "Globex", JobStatus.APPLIED);

        // When / Then
        assertThrows(DataIntegrityViolationException.class, () -> DATABASE.update(
                "INSERT INTO app.round (owner_id, job_id, round_type) VALUES (?, ?, 'OTHER')", AUTHOR, job));
    }
}
