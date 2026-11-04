package org.manu.jobhunt.application.overview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundOutcome;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit test for SituationRules: what a job's next step is, with the first matching rule winning.
 */
class SituationRulesTest {

    private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

    @Test
    @DisplayName("""
            Given a round within 48 hours and a debrief still open
            When the job is assessed
            Then the round comes first, as it can't wait
            """)
    void aRoundWithin48HoursComesFirst() {
        // Given
        final Round soon = round(hours(47), RoundOutcome.PENDING, false);

        // When
        final Assessment assessment = assess(job(JobStatus.INTERVIEWING, 10), round(hours(-24), RoundOutcome.PENDING, false), soon);

        // Then
        assertEquals(Situation.ROUND_SOON, assessment.getSituation());
        assertSame(soon, assessment.getRound());
    }

    @Test
    @DisplayName("""
            Given an open offer and a debrief still open
            When the job is assessed
            Then the offer comes first
            """)
    void anOpenOfferComesBeforeADebrief() {
        assertEquals(Situation.OFFER_OPEN,
                assess(job(JobStatus.OFFER, 10), round(hours(-24), RoundOutcome.PENDING, false)).getSituation());
    }

    @Test
    @DisplayName("""
            Given two past rounds without scores
            When the job is assessed
            Then the most recent one needs its debrief
            """)
    void aPastRoundWithoutScoresNeedsADebrief() {
        // Given
        final Round newer = round(hours(-24), RoundOutcome.PENDING, false);

        // When
        final Assessment assessment = assess(job(JobStatus.INTERVIEWING, 10), round(hours(-72), RoundOutcome.PENDING, false), newer);

        // Then
        assertEquals(Situation.NEEDS_DEBRIEF, assessment.getSituation());
        assertSame(newer, assessment.getRound());
    }

    @Test
    @DisplayName("""
            Given a past round with only one score
            When the job is assessed
            Then it still needs a debrief
            """)
    void aRoundWithOnlyOneScoreStillNeedsADebrief() {
        // Given
        final Round round = round(hours(-24), RoundOutcome.PENDING, false).toBuilder().feltScore((short) 3).build();

        // When / Then
        assertEquals(Situation.NEEDS_DEBRIEF, assess(job(JobStatus.INTERVIEWING, 10), round).getSituation());
    }

    @Test
    @DisplayName("""
            Given a cancelled round
            When the job is assessed
            Then it never needs a debrief
            """)
    void cancelledRoundsNeverNeedADebrief() {
        assertEquals(Situation.WAITING,
                assess(job(JobStatus.INTERVIEWING, 2), round(hours(-24), RoundOutcome.CANCELLED, false)).getSituation());
    }

    @Test
    @DisplayName("""
            Given a debriefed round with no result for 14 days
            When the job is assessed
            Then the result is overdue, and 13 days isn't yet
            """)
    void aResultIsOverdueAfter14Days() {
        // When
        final Assessment assessment = assess(job(JobStatus.INTERVIEWING, 30), round(days(-14), RoundOutcome.PENDING, true));

        // Then
        assertEquals(Situation.RESULT_OVERDUE, assessment.getSituation());
        assertEquals(14, assessment.getDaysSinceActivity());
        assertEquals(Situation.WAITING,
                assess(job(JobStatus.INTERVIEWING, 30), round(days(-13), RoundOutcome.PENDING, true)).getSituation());
    }

    @Test
    @DisplayName("""
            Given a passed round and nothing scheduled
            When the job is assessed
            Then the next round is missing
            """)
    void passedWithNothingScheduledNeedsTheNextRound() {
        assertEquals(Situation.PASSED_NO_NEXT,
                assess(job(JobStatus.INTERVIEWING, 10), round(days(-1), RoundOutcome.PASSED, true)).getSituation());
    }

    @Test
    @DisplayName("""
            Given a failed round while the job is open
            When the job is assessed
            Then it asks for a status update
            """)
    void failedWhileTheJobIsOpen() {
        assertEquals(Situation.FAILED_OPEN,
                assess(job(JobStatus.INTERVIEWING, 10), round(days(-1), RoundOutcome.FAILED, true)).getSituation());
    }

    @Test
    @DisplayName("""
            Given an application with no rounds
            When it's 14 days old
            Then it counts as no reply, and at 13 days it's still waiting
            """)
    void noReplyAfter14Days() {
        assertEquals(Situation.NO_REPLY, assess(job(JobStatus.APPLIED, 14)).getSituation());
        assertEquals(Situation.WAITING, assess(job(JobStatus.APPLIED, 13)).getSituation());
    }

    @Test
    @DisplayName("""
            Given a wishlist job
            When it's assessed
            Then it's not applied yet
            """)
    void wishlistIsNotApplied() {
        assertEquals(Situation.NOT_APPLIED, assess(job(JobStatus.WISHLIST, 0)).getSituation());
    }

    @Test
    @DisplayName("""
            Given a round beyond 48 hours
            When the job is assessed
            Then it's for later, and it's the round named
            """)
    void aRoundBeyond48HoursIsLater() {
        // Given
        final Round later = round(hours(49), RoundOutcome.PENDING, false);

        // When
        final Assessment assessment = assess(job(JobStatus.INTERVIEWING, 3), later);

        // Then
        assertEquals(Situation.ROUND_LATER, assessment.getSituation());
        assertSame(later, assessment.getRound());
    }

    @Test
    @DisplayName("""
            Given a passed round with the next one booked
            When the job is assessed
            Then it doesn't ask for another round
            """)
    void aPassedRoundWithTheNextBooked() {
        assertEquals(Situation.ROUND_LATER, assess(job(JobStatus.INTERVIEWING, 3),
                round(days(-3), RoundOutcome.PASSED, true), round(days(5), RoundOutcome.PENDING, false)).getSituation());
    }

    @Test
    @DisplayName("""
            Given a round with no date
            When the job is assessed
            Then it's ignored
            """)
    void roundsWithNoDateAreIgnored() {
        assertEquals(Situation.WAITING,
                assess(job(JobStatus.INTERVIEWING, 2), round(null, RoundOutcome.PENDING, false)).getSituation());
    }

    private Assessment assess(Job job, Round... rounds) {
        return SituationRules.assess(job, List.of(rounds), NOW);
    }

    private Job job(JobStatus status, int daysSinceApplied) {
        return Job.builder()
                .status(status)
                .appliedDate(LocalDate.ofInstant(NOW, ZoneOffset.UTC).minusDays(daysSinceApplied))
                .build();
    }

    private Round round(Instant when, RoundOutcome outcome, boolean debriefed) {
        return Round.builder()
                .scheduledAt(when)
                .outcome(outcome)
                .feltScore(debriefed ? (short) 4 : null)
                .difficultyScore(debriefed ? (short) 3 : null)
                .build();
    }

    private Instant hours(int count) {
        return NOW.plus(Duration.ofHours(count));
    }

    private Instant days(int count) {
        return NOW.plus(Duration.ofDays(count));
    }
}
