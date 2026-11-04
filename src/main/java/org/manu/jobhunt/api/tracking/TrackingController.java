package org.manu.jobhunt.api.tracking;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.api.tracking.dto.JobRequest;
import org.manu.jobhunt.api.tracking.dto.JobResponse;
import org.manu.jobhunt.api.tracking.dto.JobStatusRequest;
import org.manu.jobhunt.api.tracking.dto.RoundRequest;
import org.manu.jobhunt.api.tracking.dto.RoundResponse;
import org.manu.jobhunt.application.tracking.TrackingApplication;
import org.manu.jobhunt.application.tracking.command.SaveJobCommand;
import org.manu.jobhunt.application.tracking.command.SaveRoundCommand;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrackingController {

    private final TrackingApplication applicationFacade;

    // ---------------------------------------------------------------------
    // Jobs
    // ---------------------------------------------------------------------

    @GetMapping("/jobs")
    public List<JobResponse> listJobs(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return applicationFacade.listJobs(includeArchived).stream().map(JobResponse::toDto).toList();
    }

    @GetMapping("/jobs/{id}")
    public JobResponse getJob(@PathVariable Long id) {
        return JobResponse.toDto(applicationFacade.getJob(id));
    }

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@RequestBody JobRequest request) {
        return JobResponse.toDto(applicationFacade.createJob(toCommand(request)));
    }

    @PutMapping("/jobs/{id}")
    public JobResponse updateJob(@PathVariable Long id, @RequestBody JobRequest request) {
        return JobResponse.toDto(applicationFacade.updateJob(id, toCommand(request)));
    }

    @PostMapping("/jobs/{id}/status")
    public JobResponse changeJobStatus(@PathVariable Long id, @RequestBody JobStatusRequest request) {
        return JobResponse.toDto(applicationFacade.changeJobStatus(id, request.getStatus(), request.getDate()));
    }

    @PostMapping("/jobs/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveJob(@PathVariable Long id) {
        applicationFacade.archiveJob(id);
    }

    @PostMapping("/jobs/{id}/unarchive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unarchiveJob(@PathVariable Long id) {
        applicationFacade.unarchiveJob(id);
    }

    @DeleteMapping("/jobs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Long id) {
        applicationFacade.deleteJob(id);
    }

    // ---------------------------------------------------------------------
    // Rounds
    // ---------------------------------------------------------------------

    @GetMapping("/jobs/{jobId}/rounds")
    public List<RoundResponse> listRounds(@PathVariable Long jobId) {
        return applicationFacade.listRounds(jobId).stream()
                .map(RoundResponse::toDto)
                .toList();
    }

    @PostMapping("/jobs/{jobId}/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    public RoundResponse addRound(@PathVariable Long jobId, @RequestBody RoundRequest request) {
        return RoundResponse.toDto(applicationFacade.addRound(jobId, toCommand(request)));
    }

    /** The calendar's feed: every round in the range, padded a day each side. */
    @GetMapping("/rounds")
    public List<RoundResponse> listRoundsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return applicationFacade.listRoundsBetween(from, to).stream()
                .map(RoundResponse::toDto)
                .toList();
    }

    @GetMapping("/rounds/{id}")
    public RoundResponse getRound(@PathVariable Long id) {
        return RoundResponse.toDto(applicationFacade.getRound(id));
    }

    @PutMapping("/rounds/{id}")
    public RoundResponse updateRound(@PathVariable Long id, @RequestBody RoundRequest request) {
        return RoundResponse.toDto(applicationFacade.updateRound(id, toCommand(request)));
    }

    @DeleteMapping("/rounds/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRound(@PathVariable Long id) {
        applicationFacade.deleteRound(id);
    }

    private static SaveJobCommand toCommand(JobRequest request) {
        return SaveJobCommand.builder()
                .company(request.getCompany())
                .roleTitle(request.getRoleTitle())
                .jobUrl(request.getJobUrl())
                .location(request.getLocation())
                .workMode(request.getWorkMode())
                .source(request.getSource())
                .sourceNote(request.getSourceNote())
                .status(request.getStatus())
                .salaryExpectation(request.getSalaryExpectation())
                .salaryOffer(request.getSalaryOffer())
                .appliedDate(request.getAppliedDate())
                .closedDate(request.getClosedDate())
                .jobDescription(request.getJobDescription())
                .build();
    }

    private static SaveRoundCommand toCommand(RoundRequest request) {
        return SaveRoundCommand.builder()
                .type(request.getType())
                .scheduledAt(request.getScheduledAt())
                .outcome(request.getOutcome())
                .feltScore(request.getFeltScore())
                .difficultyScore(request.getDifficultyScore())
                .questionsAsked(request.getQuestionsAsked())
                .debrief(request.getDebrief())
                .build();
    }
}
