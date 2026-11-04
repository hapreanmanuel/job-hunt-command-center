package org.manu.jobhunt.api.overview;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.api.overview.dto.OverviewResponse;
import org.manu.jobhunt.api.overview.dto.SituationResponse;
import org.manu.jobhunt.application.common.exception.InvalidFieldValueException;
import org.manu.jobhunt.application.overview.OverviewApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DateTimeException;
import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OverviewController {

    private final OverviewApplication applicationFacade;

    /** @param tz defines "this week" */
    @GetMapping("/overview")
    public OverviewResponse overview(@RequestParam(defaultValue = "UTC") String tz) {
        return OverviewResponse.toDto(applicationFacade.overview(toZone(tz)));
    }

    @GetMapping("/jobs/{id}/situation")
    public SituationResponse situation(@PathVariable Long id) {
        return SituationResponse.toDto(applicationFacade.assess(id));
    }

    private static ZoneId toZone(String tz) {
        try {
            return ZoneId.of(tz);
        } catch (DateTimeException exception) {
            throw new InvalidFieldValueException("tz", "isn't a known timezone");
        }
    }
}
