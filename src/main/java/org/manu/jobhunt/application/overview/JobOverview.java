package org.manu.jobhunt.application.overview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.domain.job.Job;

/** One active job and where it stands. */
@AllArgsConstructor
@Getter
public class JobOverview {

    private final Job job;
    private final Assessment assessment;
}
