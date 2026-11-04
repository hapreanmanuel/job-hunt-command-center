package org.manu.jobhunt.application.overview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.domain.job.Job;

import java.util.List;

/** What Home shows. */
@AllArgsConstructor
@Getter
public class Overview {

    /** Most urgent first. */
    private final List<JobOverview> active;
    /** Most recently closed first. */
    private final List<Job> closed;
    private final long applicationsSentThisWeek;
    private final long roundsHeldThisWeek;
}
