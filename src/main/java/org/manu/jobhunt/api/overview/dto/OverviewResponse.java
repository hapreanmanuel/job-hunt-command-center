package org.manu.jobhunt.api.overview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.api.tracking.dto.JobResponse;
import org.manu.jobhunt.application.overview.Overview;

import java.util.List;

@AllArgsConstructor
@Getter
public class OverviewResponse {

    /** Most urgent first. */
    private List<JobOverviewResponse> active;
    private List<JobResponse> closed;
    private long applicationsSentThisWeek;
    private long roundsHeldThisWeek;

    public static OverviewResponse toDto(Overview overview) {
        return new OverviewResponse(
                overview.getActive().stream().map(JobOverviewResponse::toDto).toList(),
                overview.getClosed().stream().map(JobResponse::toDto).toList(),
                overview.getApplicationsSentThisWeek(),
                overview.getRoundsHeldThisWeek());
    }
}
