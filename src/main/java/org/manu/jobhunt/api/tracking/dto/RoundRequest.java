package org.manu.jobhunt.api.tracking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.manu.jobhunt.domain.round.RoundOutcome;
import org.manu.jobhunt.domain.round.RoundType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class RoundRequest {
    private RoundType type;
    private Instant scheduledAt;
    private RoundOutcome outcome;
    private Short feltScore;
    private Short difficultyScore;
    private String questionsAsked;
    private String debrief;
}
