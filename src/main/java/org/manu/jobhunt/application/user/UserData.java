package org.manu.jobhunt.application.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.round.Round;

import java.time.Instant;
import java.util.List;

/** Everything a user owns, for them to download. */
@AllArgsConstructor
@Getter
public class UserData {

    private final Instant exportedAt;
    private final List<Job> jobs;
    private final List<Round> rounds;
}
