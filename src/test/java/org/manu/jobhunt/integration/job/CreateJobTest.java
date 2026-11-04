package org.manu.jobhunt.integration.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.manu.jobhunt.integration.AbstractIntegrationTest;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * POST /api/jobs
 */
class CreateJobTest extends AbstractIntegrationTest {

    private static final String LISTING = "We need:\n  - Java <21>\n  - \"Spring\" & Kafka\n";

    @Test
    @DisplayName("""
            Given a company and a role
            When a signed-in user sends a POST request
            Then the job is created with HTTP 201, with defaults for what wasn't given
            """)
    void createJobReturnsCreatedWithDefaults() {
        // When
        var response = post(restClient, "/api/jobs", Map.of("company", "Acme", "roleTitle", "Engineer"));

        // Then
        response.expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.company").isEqualTo("Acme")
                .jsonPath("$.status").isEqualTo("WISHLIST")
                .jsonPath("$.workMode").isEqualTo("UNKNOWN")
                .jsonPath("$.source").isEqualTo("OTHER");

        assertEquals(1, countJobs());
    }

    @Test
    @DisplayName("""
            Given a listing with line breaks and special characters
            When the job is created and read back
            Then the listing comes back exactly as pasted
            """)
    void createJobKeepsTheListingExactlyAsPasted() {
        // When
        var response = post(restClient, "/api/jobs",
                Map.of("company", "Acme", "roleTitle", "Engineer", "jobDescription", LISTING));

        // Then
        response.expectStatus().isCreated()
                .expectBody().jsonPath("$.jobDescription").isEqualTo(LISTING);
    }

    @Test
    @DisplayName("""
            Given a salary expectation, an offer and how the job started
            When the job is created
            Then they're kept as written
            """)
    void createJobKeepsSalaryAndSourceAsWritten() {
        // When
        var response = post(restClient, "/api/jobs", Map.of(
                "company", "Acme", "roleTitle", "Engineer", "source", "RECRUITER",
                "sourceNote", "A recruiter messaged me", "salaryExpectation", "6,000 EUR net per month, B2B",
                "salaryOffer", "5,500 EUR net"));

        // Then
        response.expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.source").isEqualTo("RECRUITER")
                .jsonPath("$.sourceNote").isEqualTo("A recruiter messaged me")
                .jsonPath("$.salaryExpectation").isEqualTo("6,000 EUR net per month, B2B")
                .jsonPath("$.salaryOffer").isEqualTo("5,500 EUR net");
    }

    @Test
    @DisplayName("""
            Given a wishlist job with an applied date
            When it's created
            Then the date is dropped: a job you haven't applied to has no applied date
            """)
    void createJobDropsTheAppliedDateOfAWishlistJob() {
        // When
        var response = post(restClient, "/api/jobs", Map.of(
                "company", "Acme", "roleTitle", "Engineer", "status", "WISHLIST", "appliedDate", "2026-11-01"));

        // Then
        response.expectStatus().isCreated()
                .expectBody().jsonPath("$.appliedDate").doesNotExist();
    }

    @Test
    @DisplayName("""
            Given a signed-in user
            When they create a job
            Then it belongs to them, because the owner comes from the session
            """)
    void createJobTakesTheOwnerFromTheSession() {
        // When
        final Long id = post(restClient, "/api/jobs", Map.of("company", "Acme", "roleTitle", "Engineer"))
                .expectBody(IdOnly.class).returnResult().getResponseBody().id;

        // Then
        assertEquals(AUTHOR.toString(), jobColumn(id, "owner_id"));
    }

    @Test
    @DisplayName("""
            Given no company
            When a POST request is sent
            Then HTTP 400 is returned, saying what's missing, and nothing is stored
            """)
    void createJobReturnsBadRequestWithoutCompany() {
        // When
        var response = post(restClient, "/api/jobs", Map.of("company", "", "roleTitle", "Engineer"));

        // Then
        response.expectStatus().isBadRequest()
                .expectBody().jsonPath("$.message").isEqualTo("company is required");

        assertEquals(0, countJobs());
    }

    @Test
    @DisplayName("""
            Given a link that isn't http or https
            When a POST request is sent
            Then HTTP 400 is returned, so a javascript: link can never reach the page
            """)
    void createJobReturnsBadRequestForAnUnsafeLink() {
        // When
        var response = post(restClient, "/api/jobs",
                Map.of("company", "Acme", "roleTitle", "Engineer", "jobUrl", "javascript:alert(1)"));

        // Then
        response.expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("""
            Given an unknown status
            When a POST request is sent
            Then HTTP 400 is returned
            """)
    void createJobReturnsBadRequestForAnUnknownStatus() {
        // When
        var response = post(restClient, "/api/jobs", Map.of("company", "Acme", "roleTitle", "Engineer", "status", "NOPE"));

        // Then
        response.expectStatus().isBadRequest();
    }

    private static class IdOnly {
        public Long id;
    }

    @Test
    @DisplayName("""
            Given a job marked as applied, or as closed, with no date
            When the POST request is sent
            Then the date defaults to today, the same as a status change
            """)
    void createJobDefaultsTheDatesOfItsStatus() {
        // When
        var applied = post(restClient, "/api/jobs",
                Map.of("company", "Acme", "roleTitle", "Engineer", "status", "APPLIED"));
        var rejected = post(restClient, "/api/jobs",
                Map.of("company", "Globex", "roleTitle", "Engineer", "status", "REJECTED"));

        // Then
        applied.expectBody().jsonPath("$.appliedDate").isEqualTo(LocalDate.now().toString());
        rejected.expectBody().jsonPath("$.closedDate").isEqualTo(LocalDate.now().toString());
    }
}
