package org.manu.jobhunt.application.tracking;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.application.common.CurrentUserProvider;
import org.manu.jobhunt.application.tracking.command.SaveJobCommand;
import org.manu.jobhunt.application.tracking.command.SaveRoundCommand;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobRepository;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.manu.jobhunt.application.common.Validations.requireNonNull;

/**
 * Entry point of the tracking feature: the only class the API layer uses for jobs and rounds.
 * <p>
 * Each use case runs its steps in order: find (404), then validate (400), then write. It's the
 * only place that reads the current user, and every repository call carries it. Reading another
 * user's job finds nothing, so a 404 here covers both "gone" and "not yours".
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TrackingApplication {

    private final JobService jobService;
    private final RoundService roundService;
    private final JobRepository jobRepository;
    private final RoundRepository roundRepository;
    private final CurrentUserProvider userProvider;

    // ---------------------------------------------------------------------
    // Jobs
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Job> listJobs(boolean includeArchived) {
        return jobRepository.findAll(userProvider.userId(), includeArchived);
    }

    @Transactional(readOnly = true)
    public Job getJob(Long id) {
        return jobService.requireJob(id, userProvider.userId());
    }

    public Job createJob(SaveJobCommand command) {
        jobService.validate(command);
        return jobRepository.save(jobService.build(command, today()), userProvider.userId());
    }

    /** Edits the details. The status only changes through changeJobStatus, which keeps its dates right. */
    public Job updateJob(Long id, SaveJobCommand command) {
        final Job job = jobService.requireJob(id, userProvider.userId());
        jobService.validate(command);
        return jobRepository.update(jobService.applied(job, command), userProvider.userId());
    }

    /** @param date when it happened, for the dates that go with the status; defaults to today */
    public Job changeJobStatus(Long id, JobStatus status, LocalDate date) {
        final Job job = jobService.requireJob(id, userProvider.userId());
        requireNonNull(status, "status");
        return jobRepository.update(jobService.withStatus(job, status, date, today()), userProvider.userId());
    }

    /** Hides a job and its rounds from Home and the calendar. Nothing is lost, and the export keeps them. */
    public void archiveJob(Long id) {
        setArchived(id, true);
    }

    public void unarchiveJob(Long id) {
        setArchived(id, false);
    }

    /** For good: the database removes its rounds with it. Archiving is the way to keep it. */
    public void deleteJob(Long id) {
        jobService.requireExists(id, userProvider.userId());
        jobRepository.deleteById(id, userProvider.userId());
    }

    private void setArchived(Long id, boolean archived) {
        final Job job = jobService.requireJob(id, userProvider.userId());
        jobRepository.update(job.toBuilder().archived(archived).build(), userProvider.userId());
    }

    // ---------------------------------------------------------------------
    // Rounds
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Round> listRounds(Long jobId) {
        jobService.requireExists(jobId, userProvider.userId());
        return roundRepository.findByJobId(jobId, userProvider.userId());
    }

    /**
     * Rounds in a date range, padded a day each side: the server doesn't know the viewer's
     * timezone, so the browser does the final placement on local dates.
     */
    @Transactional(readOnly = true)
    public List<Round> listRoundsBetween(LocalDate from, LocalDate to) {
        return roundRepository.findScheduledBetween(
                from.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                to.plusDays(2).atStartOfDay(ZoneOffset.UTC).toInstant(),
                userProvider.userId());
    }

    @Transactional(readOnly = true)
    public Round getRound(Long id) {
        return roundService.requireRound(id, userProvider.userId());
    }

    /** A first round means the process has started, so nobody has to say so. */
    public Round addRound(Long jobId, SaveRoundCommand command) {
        final Job job = jobService.requireJob(jobId, userProvider.userId());
        roundService.validate(command);
        if (job.getStatus() == JobStatus.WISHLIST || job.getStatus() == JobStatus.APPLIED) {
            jobRepository.update(job.toBuilder().status(JobStatus.INTERVIEWING).build(), userProvider.userId());
        }
        return roundRepository.save(roundService.build(command), jobId, userProvider.userId());
    }

    /** Also how a debrief is saved: it's part of the round. */
    public Round updateRound(Long id, SaveRoundCommand command) {
        final Round round = roundService.requireRound(id, userProvider.userId());
        roundService.validate(command);
        return roundRepository.update(roundService.applied(round, command), userProvider.userId());
    }

    public void deleteRound(Long id) {
        roundService.requireRound(id, userProvider.userId());
        roundRepository.deleteById(id, userProvider.userId());
    }

    private static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}
