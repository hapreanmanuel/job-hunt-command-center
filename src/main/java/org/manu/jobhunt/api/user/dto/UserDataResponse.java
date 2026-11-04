package org.manu.jobhunt.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.api.tracking.dto.JobResponse;
import org.manu.jobhunt.api.tracking.dto.RoundResponse;
import org.manu.jobhunt.application.user.UserData;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Getter
public class UserDataResponse {

    private Instant exportedAt;
    private List<JobResponse> jobs;
    private List<RoundResponse> rounds;

    public static UserDataResponse toDto(UserData data) {
        return new UserDataResponse(
                data.getExportedAt(),
                data.getJobs().stream().map(JobResponse::toDto).toList(),
                data.getRounds().stream().map(RoundResponse::toDto).toList());
    }
}
