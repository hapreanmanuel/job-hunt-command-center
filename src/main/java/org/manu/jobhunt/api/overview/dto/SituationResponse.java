package org.manu.jobhunt.api.overview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.api.tracking.dto.RoundResponse;
import org.manu.jobhunt.application.overview.Assessment;
import org.manu.jobhunt.application.overview.Situation;


@AllArgsConstructor
@Getter
public class SituationResponse {

    private Situation situation;
    private RoundResponse round;
    private int daysSinceActivity;

    public static SituationResponse toDto(Assessment assessment) {
        final RoundResponse round = assessment.getRound() != null
                ? RoundResponse.toDto(assessment.getRound())
                : null;
        return new SituationResponse(assessment.getSituation(), round, assessment.getDaysSinceActivity());
    }
}
