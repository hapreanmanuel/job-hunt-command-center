package org.manu.jobhunt.application.overview;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundOutcome;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

/**
 * Decides a job's situation. A pure function, with no Spring or database, so the rules are
 * easy to read and test. The first matching rule wins, so the order below is the priority:
 * what needs the user soonest comes first. {@link Situation} says what each outcome means.
 * <p>
 * Cancelled rounds and rounds without a date are ignored. A round is "next" until its start time
 * passes, and "past" after that.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SituationRules {

    private static final Duration SOON = Duration.ofHours(48);
    private static final int NO_REPLY_DAYS = 14;

    static Assessment assess(Job job, List<Round> rounds, Instant now) {
        final List<Round> dated = rounds.stream()
                .filter(round -> round.getScheduledAt() != null && !round.isCancelled())
                .sorted(Comparator.comparing(Round::getScheduledAt))
                .toList();
        final Round next = dated.stream().filter(round -> !round.isPast(now)).findFirst().orElse(null);
        final List<Round> past = dated.stream().filter(round -> round.isPast(now)).toList();
        final Round last = past.isEmpty() ? null : past.getLast();
        final int days = daysSinceActivity(job, last, now);

        if (next != null && !next.getScheduledAt().isAfter(now.plus(SOON))) {
            return new Assessment(Situation.ROUND_SOON, next, days);
        }
        if (job.getStatus() == JobStatus.OFFER) {
            return new Assessment(Situation.OFFER_OPEN, last, days);
        }
        for (Round round : past.reversed()) {
            if (round.needsDebrief(now)) {
                return new Assessment(Situation.NEEDS_DEBRIEF, round, days);
            }
        }
        if (next == null && last != null) {
            if (last.getOutcome() == RoundOutcome.PENDING && days >= NO_REPLY_DAYS) {
                return new Assessment(Situation.RESULT_OVERDUE, last, days);
            }
            if (last.getOutcome() == RoundOutcome.PASSED) {
                return new Assessment(Situation.PASSED_NO_NEXT, last, days);
            }
            if (last.getOutcome() == RoundOutcome.FAILED) {
                return new Assessment(Situation.FAILED_OPEN, last, days);
            }
        }
        if (dated.isEmpty() && days >= NO_REPLY_DAYS
                && (job.getStatus() == JobStatus.APPLIED || job.getStatus() == JobStatus.INTERVIEWING)) {
            return new Assessment(Situation.NO_REPLY, null, days);
        }
        if (job.getStatus() == JobStatus.WISHLIST) {
            return new Assessment(Situation.NOT_APPLIED, null, days);
        }
        if (next != null) {
            return new Assessment(Situation.ROUND_LATER, next, days);
        }
        return new Assessment(Situation.WAITING, last, days);
    }

    /** Counted from the last round, else the applied date, else when the job was added. */
    private static int daysSinceActivity(Job job, Round last, Instant now) {
        Instant since = now;
        if (last != null) {
            since = last.getScheduledAt();
        } else if (job.getAppliedDate() != null) {
            since = job.getAppliedDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        } else if (job.getCreatedAt() != null) {
            since = job.getCreatedAt();
        }
        return (int) Math.max(0, Duration.between(since, now).toDays());
    }
}
