package org.manu.jobhunt.application.overview;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.application.common.CurrentUserProvider;
import org.manu.jobhunt.application.tracking.exception.JobNotFoundException;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobRepository;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only views across a user's jobs: the only class the API layer uses for them. Nothing here
 * is stored: situations are worked out from jobs and rounds on every request, so they can't go stale.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OverviewApplication {

    private static final Comparator<JobOverview> BY_URGENCY = Comparator
            .comparing((JobOverview overview) -> overview.getAssessment().getSituation())
            .thenComparing(OverviewApplication::roundTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(overview -> overview.getJob().getId());

    private final JobRepository jobRepository;
    private final RoundRepository roundRepository;
    private final CurrentUserProvider userProvider;

    /**
     * Home: open jobs by urgency, closed jobs by date, and this week's counts. Archived jobs and
     * their rounds are left out of all of it.
     *
     * @param zone defines "this week", which starts on Monday
     */
    public Overview overview(ZoneId zone) {
        final UUID ownerId = userProvider.userId();
        final Instant now = Instant.now();
        final List<Job> jobs = jobRepository.findAll(ownerId, false);
        final List<Round> rounds = roundRepository.findAll(ownerId, false);
        final Map<Long, List<Round>> roundsByJob = rounds.stream()
                .collect(Collectors.groupingBy(round -> round.getJob().getId()));

        final List<JobOverview> active = jobs.stream()
                .filter(job -> !job.getStatus().isClosed())
                .map(job -> new JobOverview(job,
                        SituationRules.assess(job, roundsByJob.getOrDefault(job.getId(), List.of()), now)))
                .sorted(BY_URGENCY)
                .toList();
        final List<Job> closed = jobs.stream()
                .filter(job -> job.getStatus().isClosed())
                .sorted(Comparator.comparing(Job::getClosedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        final LocalDate monday = now.atZone(zone).toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        final Instant weekStart = monday.atStartOfDay(zone).toInstant();
        final long applied = jobs.stream()
                .filter(job -> job.getAppliedDate() != null && !job.getAppliedDate().isBefore(monday))
                .count();
        final long held = rounds.stream()
                .filter(round -> round.isPast(now) && !round.isCancelled() && !round.getScheduledAt().isBefore(weekStart))
                .count();

        return new Overview(active, closed, applied, held);
    }

    /** The next step shown on a job's page. */
    public Assessment assess(Long jobId) {
        final UUID ownerId = userProvider.userId();
        final Job job = jobRepository.findById(jobId, ownerId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        return SituationRules.assess(job, roundRepository.findByJobId(jobId, ownerId), Instant.now());
    }

    private static Instant roundTime(JobOverview overview) {
        final Round round = overview.getAssessment().getRound();
        return round != null ? round.getScheduledAt() : null;
    }
}
