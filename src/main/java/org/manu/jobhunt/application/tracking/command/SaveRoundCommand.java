package org.manu.jobhunt.application.tracking.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.manu.jobhunt.domain.round.RoundOutcome;
import org.manu.jobhunt.domain.round.RoundType;

import java.time.Instant;

@AllArgsConstructor
@Builder
@Getter
public class SaveRoundCommand {

    private RoundType type;
    private Instant scheduledAt;
    /** Optional: defaults to PENDING. */
    private RoundOutcome outcome;
    private Short feltScore;
    private Short difficultyScore;
    private String questionsAsked;
    private String debrief;
}
