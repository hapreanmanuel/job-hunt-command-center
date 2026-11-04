package org.manu.jobhunt.domain.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/** One job: an application at one company, which rounds hang off of. */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
public class Job {

    private Long id;
    private String company;
    private String roleTitle;
    /** Posting links expire, so the listing itself is kept too. */
    private String jobUrl;
    private String location;
    private WorkMode workMode;
    private JobSource source;
    /** Free text next to the source, such as who got in touch. */
    private String sourceNote;
    private JobStatus status;
    /** Free text, because gross or net, period and contract type all matter. */
    private String salaryExpectation;
    private String salaryOffer;
    private LocalDate appliedDate;
    private LocalDate closedDate;
    /** The listing exactly as pasted. The app doesn't process it. */
    private String jobDescription;
    private boolean archived;
    private Instant createdAt;
    private Instant updatedAt;
}
