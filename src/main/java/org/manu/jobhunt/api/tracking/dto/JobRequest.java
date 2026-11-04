package org.manu.jobhunt.api.tracking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.manu.jobhunt.domain.job.JobSource;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.job.WorkMode;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JobRequest {
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
}
