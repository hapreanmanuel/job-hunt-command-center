package org.manu.jobhunt.api.tracking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.manu.jobhunt.domain.job.JobStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JobStatusRequest {
    private JobStatus status;
    /** The applied or closed date, depending on the status. Defaults to today. */
    private LocalDate date;
}
