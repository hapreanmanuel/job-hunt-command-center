package org.manu.jobhunt.domain.round;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.manu.jobhunt.domain.job.Job;

import java.time.Instant;

/** One round of a job's process, with its debrief. For a take-home, scheduledAt is the due date. */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
public class Round {

    private Long id;
    private Job job;
    private RoundType type;
    private Instant scheduledAt;
    private RoundOutcome outcome;
    private Short feltScore;
    private Short difficultyScore;
    private String questionsAsked;
    private String debrief;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isDebriefed() {
        return feltScore != null && difficultyScore != null;
    }

    public boolean isCancelled() {
        return outcome == RoundOutcome.CANCELLED;
    }

    public boolean isPast(Instant now) {
        return scheduledAt != null && scheduledAt.isBefore(now);
    }

    public boolean needsDebrief(Instant now) {
        return isPast(now) && !isCancelled() && !isDebriefed();
    }

}
