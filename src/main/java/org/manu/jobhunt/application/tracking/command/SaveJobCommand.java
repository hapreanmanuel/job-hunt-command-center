package org.manu.jobhunt.application.tracking.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.job.WorkMode;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Getter
public class SaveJobCommand {

    private String company;
    private String roleTitle;
    private String jobUrl;
    private String location;
    /** Optional: defaults to UNKNOWN. */
    private WorkMode workMode;
    /** Only when creating, and optional: defaults to APPLIED. */
    private JobStatus status;
    private String salaryExpectation;
    private String salaryOffer;
    /** When creating, defaults to today for a job marked Applied. */
    private LocalDate appliedDate;
    /** Only when creating a closed job, and optional: defaults to today. */
    private LocalDate closedDate;
    private String jobDescription;
}
