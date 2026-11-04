package org.manu.jobhunt.api.overview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.api.tracking.dto.JobResponse;
import org.manu.jobhunt.application.overview.JobOverview;


@AllArgsConstructor
@Getter
public class JobOverviewResponse {

    private JobResponse job;
    private SituationResponse situation;

    public static JobOverviewResponse toDto(JobOverview overview) {
        return new JobOverviewResponse(
                JobResponse.toDto(overview.getJob()),
                SituationResponse.toDto(overview.getAssessment()));
    }
}
