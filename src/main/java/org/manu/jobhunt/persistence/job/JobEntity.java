package org.manu.jobhunt.persistence.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobSource;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.job.WorkMode;
import org.manu.jobhunt.persistence.AuditedEntity;

import java.time.LocalDate;

@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor
public class JobEntity extends AuditedEntity {

    @Column(nullable = false, length = 200)
    private String company;

    @Column(name = "role_title", nullable = false, length = 200)
    private String roleTitle;

    @Column(name = "job_url", length = 1000)
    private String jobUrl;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 20)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobSource source;

    @Column(name = "source_note", length = 300)
    private String sourceNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobStatus status;

    @Column(name = "salary_expectation", length = 200)
    private String salaryExpectation;

    @Column(name = "salary_offer", length = 200)
    private String salaryOffer;

    @Column(name = "applied_date")
    private LocalDate appliedDate;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    @Column(name = "job_description", columnDefinition = "text")
    private String jobDescription;

    @Column(nullable = false)
    private boolean archived;

    void copyFrom(Job job) {
        company = job.getCompany();
        roleTitle = job.getRoleTitle();
        jobUrl = job.getJobUrl();
        location = job.getLocation();
        workMode = job.getWorkMode();
        source = job.getSource();
        sourceNote = job.getSourceNote();
        status = job.getStatus();
        salaryExpectation = job.getSalaryExpectation();
        salaryOffer = job.getSalaryOffer();
        appliedDate = job.getAppliedDate();
        closedDate = job.getClosedDate();
        jobDescription = job.getJobDescription();
        archived = job.isArchived();
    }

    public Job toDomain() {
        return Job.builder()
                .id(getId())
                .company(company)
                .roleTitle(roleTitle)
                .jobUrl(jobUrl)
                .location(location)
                .workMode(workMode)
                .source(source)
                .sourceNote(sourceNote)
                .status(status)
                .salaryExpectation(salaryExpectation)
                .salaryOffer(salaryOffer)
                .appliedDate(appliedDate)
                .closedDate(closedDate)
                .jobDescription(jobDescription)
                .archived(archived)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
