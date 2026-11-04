package org.manu.jobhunt.api.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobSource;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.job.WorkMode;

import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Getter
public class JobResponse {

    private static final int COLOR_COUNT = 12;

    private Long id;
    private String company;
    private String roleTitle;
    private String jobUrl;
    private String location;
    private WorkMode workMode;
    private JobSource source;
    private String sourceNote;
    private JobStatus status;
    private String salaryExpectation;
    private String salaryOffer;
    private LocalDate appliedDate;
    private LocalDate closedDate;
    private String jobDescription;
    private int colorIndex;
    private boolean archived;
    private Instant createdAt;
    private Instant updatedAt;

    public static JobResponse toDto(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .company(job.getCompany())
                .roleTitle(job.getRoleTitle())
                .jobUrl(job.getJobUrl())
                .location(job.getLocation())
                .workMode(job.getWorkMode())
                .source(job.getSource())
                .sourceNote(job.getSourceNote())
                .status(job.getStatus())
                .salaryExpectation(job.getSalaryExpectation())
                .salaryOffer(job.getSalaryOffer())
                .appliedDate(job.getAppliedDate())
                .closedDate(job.getClosedDate())
                .jobDescription(job.getJobDescription())
                .colorIndex(colorIndexOf(job))
                .archived(job.isArchived())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    /** Which of the fixed colors in theme.css the job gets, here and on its rounds. */
    static int colorIndexOf(Job job) {
        return (int) (job.getId() % COLOR_COUNT);
    }
}
