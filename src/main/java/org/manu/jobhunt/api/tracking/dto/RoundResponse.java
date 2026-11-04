package org.manu.jobhunt.api.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundOutcome;
import org.manu.jobhunt.domain.round.RoundType;

import java.time.Instant;

@AllArgsConstructor
@Builder
@Getter
public class RoundResponse {

    private Long id;
    private Long jobId;
    private String company;
    private String roleTitle;
    private int colorIndex;
    private RoundType type;
    private Instant scheduledAt;
    private RoundOutcome outcome;
    private Short feltScore;
    private Short difficultyScore;
    private String questionsAsked;
    private String debrief;
    private Instant createdAt;
    private Instant updatedAt;

    public static RoundResponse toDto(Round round) {
        return RoundResponse.builder()
                .id(round.getId())
                .jobId(round.getJob().getId())
                .company(round.getJob().getCompany())
                .roleTitle(round.getJob().getRoleTitle())
                .colorIndex(JobResponse.colorIndexOf(round.getJob()))
                .type(round.getType())
                .scheduledAt(round.getScheduledAt())
                .outcome(round.getOutcome())
                .feltScore(round.getFeltScore())
                .difficultyScore(round.getDifficultyScore())
                .questionsAsked(round.getQuestionsAsked())
                .debrief(round.getDebrief())
                .createdAt(round.getCreatedAt())
                .updatedAt(round.getUpdatedAt())
                .build();
    }
}
