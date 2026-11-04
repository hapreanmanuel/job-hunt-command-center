package org.manu.jobhunt.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.manu.jobhunt.domain.job.JobStatus;
import org.manu.jobhunt.domain.round.RoundOutcome;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.security.SignedInUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Base for integration tests: the real application on a random port, a real Postgres container,
 * and RestTestClient for HTTP calls.
 * <p>
 * Each subclass covers ONE endpoint. A test arranges the database directly with the helpers below
 * (independent of the API under test), calls that single endpoint, then checks the response and
 * the database. Every table is emptied after each test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest extends AbstractDatabaseTest {

    protected static final UUID AUTHOR = UUID.fromString("00000000-0000-0000-0000-000000001001");
    protected static final UUID OTHER_USER = UUID.fromString("00000000-0000-0000-0000-000000001002");

    private static final String SESSION_COOKIE = "SESSION";
    private static final String SECURITY_CONTEXT_ATTRIBUTE = "SPRING_SECURITY_CONTEXT";

    /** Signed in as {@link #AUTHOR}. */
    protected RestTestClient restClient;
    /** A second browser, signed in as {@link #OTHER_USER}. */
    protected RestTestClient otherClient;

    @Autowired
    private RestTestClient baseRestClient;
    @Autowired
    private SessionRepository<? extends Session> sessionRepository;

    @BeforeEach
    void startSignedInSessions() {
        insertUser(AUTHOR);
        insertUser(OTHER_USER);
        restClient = clientSignedInAs(AUTHOR);
        otherClient = clientSignedInAs(OTHER_USER);
    }

    @AfterEach
    void cleanDatabase() {
        DATABASE.execute("TRUNCATE TABLE app.round, app.job, app.app_user, app.spring_session RESTART IDENTITY CASCADE");
    }

    // ---------------------------------------------------------------------
    // Signing in
    // ---------------------------------------------------------------------

    /** A client with no session cookie at all, like an uptime monitor or a first visit. */
    protected RestTestClient clientWithoutSession() {
        return baseRestClient;
    }

    /** Opens a session as after a Google sign-in, then fetches its CSRF token the way pages do. */
    protected RestTestClient clientSignedInAs(UUID userId) {
        final String sessionId = createSession(sessionRepository, signedInContext(userId));
        // Spring Session's cookie holds the session id, Base64-encoded.
        final String cookie = Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8));

        final Map<String, String> csrf = baseRestClient
                .get()
                .uri("/api/csrf")
                .cookie(SESSION_COOKIE, cookie)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .returnResult()
                .getResponseBody();

        return baseRestClient.mutate()
                .defaultCookie(SESSION_COOKIE, cookie)
                .defaultHeader(csrf.get("headerName"), csrf.get("token"))
                .build();
    }

    private SecurityContext signedInContext(UUID userId) {
        final SignedInUser principal = new SignedInUser(userId);
        return new SecurityContextImpl(new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google"));
    }

    private <S extends Session> String createSession(SessionRepository<S> repository, SecurityContext context) {
        final S session = repository.createSession();
        session.setAttribute(SECURITY_CONTEXT_ATTRIBUTE, context);
        repository.save(session);
        return session.getId();
    }

    // ---------------------------------------------------------------------
    // Requests
    // ---------------------------------------------------------------------

    protected RestTestClient.ResponseSpec post(RestTestClient client, String path, Object body) {
        return client.post().uri(path).contentType(MediaType.APPLICATION_JSON).body(body).exchange();
    }

    protected RestTestClient.ResponseSpec put(RestTestClient client, String path, Object body) {
        return client.put().uri(path).contentType(MediaType.APPLICATION_JSON).body(body).exchange();
    }

    // ---------------------------------------------------------------------
    // Arranging data
    // ---------------------------------------------------------------------

    protected void insertUser(UUID id) {
        DATABASE.update("INSERT INTO app.app_user (id) VALUES (?) ON CONFLICT DO NOTHING", id);
    }

    protected Long insertJob(UUID owner, String company, JobStatus status) {
        return insertJob(owner, company, status, status == JobStatus.WISHLIST ? null : LocalDate.now());
    }

    protected Long insertJob(UUID owner, String company, JobStatus status, LocalDate appliedDate) {
        return DATABASE.queryForObject(
                """
                        INSERT INTO app.job (owner_id, company, role_title, status, applied_date)
                        VALUES (?, ?, 'Engineer', ?, ?)
                        RETURNING id
                        """,
                Long.class, owner, company, status.name(), appliedDate);
    }

    protected Long insertRound(UUID owner, Long jobId, RoundType type, Instant scheduledAt) {
        return insertRound(owner, jobId, type, scheduledAt, RoundOutcome.PENDING);
    }

    protected Long insertRound(UUID owner, Long jobId, RoundType type, Instant scheduledAt, RoundOutcome outcome) {
        return DATABASE.queryForObject(
                """
                        INSERT INTO app.round (owner_id, job_id, round_type, scheduled_at, outcome)
                        VALUES (?, ?, ?, ?, ?)
                        RETURNING id
                        """,
                Long.class, owner, jobId, type.name(),
                scheduledAt == null ? null : Timestamp.from(scheduledAt), outcome.name());
    }

    // ---------------------------------------------------------------------
    // Checking data
    // ---------------------------------------------------------------------

    protected String jobColumn(Long id, String column) {
        return DATABASE.queryForObject("SELECT %s::text FROM app.job WHERE id = ?".formatted(column), String.class, id);
    }

    protected String roundColumn(Long id, String column) {
        return DATABASE.queryForObject("SELECT %s::text FROM app.round WHERE id = ?".formatted(column), String.class, id);
    }

    protected int countJobs() {
        return DATABASE.queryForObject("SELECT count(*) FROM app.job", Integer.class);
    }

    protected int countRounds() {
        return DATABASE.queryForObject("SELECT count(*) FROM app.round", Integer.class);
    }
}
