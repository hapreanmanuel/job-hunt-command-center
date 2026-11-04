package org.manu.jobhunt.application.tracking;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.application.tracking.command.SaveJobCommand;
import org.manu.jobhunt.application.tracking.exception.JobNotFoundException;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobRepository;
import org.manu.jobhunt.domain.job.JobSource;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.job.WorkMode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;
import static org.manu.jobhunt.application.common.Validations.requireMaxLength;
import static org.manu.jobhunt.application.common.Validations.requireNonBlank;
import static org.manu.jobhunt.application.common.Validations.requireWebAddress;

/**
 * Job validation and rules, and building jobs from commands. Package-private: only
 * TrackingApplication uses it. Today is passed in, so the date rules can be tested on a fixed day.
 */
@Service
@RequiredArgsConstructor
class JobService {

    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_URL_LENGTH = 1_000;
    private static final int MAX_SOURCE_NOTE_LENGTH = 300;
    private static final int MAX_DESCRIPTION_LENGTH = 50_000;

    private final JobRepository jobRepository;

    Job requireJob(Long id, UUID ownerId) {
        return jobRepository.findById(id, ownerId).orElseThrow(() -> new JobNotFoundException(id));
    }

    void requireExists(Long id, UUID ownerId) {
        if (!jobRepository.existsById(id, ownerId)) {
            throw new JobNotFoundException(id);
        }
    }

    void validate(SaveJobCommand command) {
        requireNonBlank(command.getCompany(), "company");
        requireMaxLength(command.getCompany(), MAX_NAME_LENGTH, "company");
        requireNonBlank(command.getRoleTitle(), "roleTitle");
        requireMaxLength(command.getRoleTitle(), MAX_NAME_LENGTH, "roleTitle");
        requireMaxLength(command.getJobUrl(), MAX_URL_LENGTH, "jobUrl");
        requireWebAddress(command.getJobUrl(), "jobUrl");
        requireMaxLength(command.getLocation(), MAX_NAME_LENGTH, "location");
        requireMaxLength(command.getSourceNote(), MAX_SOURCE_NOTE_LENGTH, "sourceNote");
        requireMaxLength(command.getSalaryExpectation(), MAX_NAME_LENGTH, "salaryExpectation");
        requireMaxLength(command.getSalaryOffer(), MAX_NAME_LENGTH, "salaryOffer");
        requireMaxLength(command.getJobDescription(), MAX_DESCRIPTION_LENGTH, "jobDescription");
    }

    /** A new job. Its dates follow the same rules as any other move to that status. */
    Job build(SaveJobCommand command, LocalDate today) {
        final JobStatus status = command.getStatus() != null ? command.getStatus() : JobStatus.WISHLIST;
        final Job job = withDetails(Job.builder(), command).appliedDate(command.getAppliedDate()).build();
        return withStatus(job, status, status.isClosed() ? command.getClosedDate() : null, today);
    }

    /** The given job with the command's details. Its status and closed date stay as they are. */
    Job applied(Job job, SaveJobCommand command) {
        final LocalDate appliedDate = job.getStatus() == JobStatus.WISHLIST ? null : command.getAppliedDate();
        return withDetails(job.toBuilder(), command).appliedDate(appliedDate).build();
    }

    /**
     * Moves a job to a new status with the date that goes with it, which defaults to today.
     * A wishlist job has no applied date, and only a closed job has a closed date.
     */
    Job withStatus(Job job, JobStatus status, LocalDate date, LocalDate today) {
        final Job.JobBuilder updated = job.toBuilder().status(status);
        if (status == JobStatus.APPLIED) {
            updated.appliedDate(requireNonNullElse(date, requireNonNullElse(job.getAppliedDate(), today)));
        }
        if (status == JobStatus.WISHLIST) {
            updated.appliedDate(null);
        }
        return updated.closedDate(status.isClosed() ? requireNonNullElse(date, today) : null).build();
    }

    private Job.JobBuilder withDetails(Job.JobBuilder builder, SaveJobCommand command) {
        return builder
                .company(command.getCompany())
                .roleTitle(command.getRoleTitle())
                .jobUrl(command.getJobUrl())
                .location(command.getLocation())
                .workMode(command.getWorkMode() != null ? command.getWorkMode() : WorkMode.UNKNOWN)
                .source(command.getSource() != null ? command.getSource() : JobSource.OTHER)
                .sourceNote(command.getSourceNote())
                .salaryExpectation(command.getSalaryExpectation())
                .salaryOffer(command.getSalaryOffer())
                .jobDescription(command.getJobDescription());
    }
}
