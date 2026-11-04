package org.manu.jobhunt.application.tracking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit test for JobService: the dates that go with a status, on a fixed "today".
 */
class JobServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);
    private static final LocalDate EARLIER = LocalDate.of(2026, 3, 1);

    private final JobService jobService = new JobService(null);

    @Test
    @DisplayName("""
            Given a wishlist job
            When it moves to Applied without a date
            Then the applied date is today
            """)
    void applyingDefaultsToToday() {
        // Given
        final Job job = Job.builder().status(JobStatus.WISHLIST).build();

        // When
        final Job applied = jobService.withStatus(job, JobStatus.APPLIED, null, TODAY);

        // Then
        assertEquals(TODAY, applied.getAppliedDate());
    }

    @Test
    @DisplayName("""
            Given an open job
            When it's rejected with a given date, or without one
            Then the closed date is that date, or today
            """)
    void closingUsesTheGivenDateOrToday() {
        // Given
        final Job job = Job.builder().status(JobStatus.INTERVIEWING).appliedDate(EARLIER).build();

        // When / Then
        assertEquals(EARLIER, jobService.withStatus(job, JobStatus.REJECTED, EARLIER, TODAY).getClosedDate());
        assertEquals(TODAY, jobService.withStatus(job, JobStatus.REJECTED, null, TODAY).getClosedDate());
    }

    @Test
    @DisplayName("""
            Given a closed job
            When it's reopened
            Then it no longer has a closed date
            """)
    void reopeningClearsTheClosedDate() {
        // Given
        final Job job = Job.builder().status(JobStatus.REJECTED).appliedDate(EARLIER).closedDate(EARLIER).build();

        // When
        final Job reopened = jobService.withStatus(job, JobStatus.INTERVIEWING, null, TODAY);

        // Then
        assertNull(reopened.getClosedDate());
        assertEquals(EARLIER, reopened.getAppliedDate());
    }

    @Test
    @DisplayName("""
            Given an applied job
            When it moves back to the wishlist
            Then it has no applied date: a wishlist job hasn't been applied to
            """)
    void theWishlistHasNoAppliedDate() {
        // Given
        final Job job = Job.builder().status(JobStatus.APPLIED).appliedDate(EARLIER).build();

        // When
        final Job wishlist = jobService.withStatus(job, JobStatus.WISHLIST, null, TODAY);

        // Then
        assertNull(wishlist.getAppliedDate());
    }
}
