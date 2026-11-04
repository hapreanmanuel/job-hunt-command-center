-- Every table of user data carries its owner. Enums are varchar + CHECK, so adding a value
-- is a plain constraint change.

CREATE TABLE app.app_user (
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE app.job (
    id                  BIGSERIAL PRIMARY KEY,
    owner_id            UUID NOT NULL REFERENCES app.app_user (id) ON DELETE CASCADE,
    company             VARCHAR(200) NOT NULL,
    role_title          VARCHAR(200) NOT NULL,
    job_url             VARCHAR(1000),
    location            VARCHAR(200),
    work_mode           VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN'
                            CHECK (work_mode IN ('REMOTE', 'HYBRID', 'ONSITE', 'UNKNOWN')),
    source              VARCHAR(30) NOT NULL DEFAULT 'OTHER'
                            CHECK (source IN ('FOUND_MYSELF', 'RECRUITER', 'REFERRAL', 'OTHER')),
    source_note         VARCHAR(300),
    status              VARCHAR(30) NOT NULL DEFAULT 'WISHLIST'
                            CHECK (status IN ('WISHLIST', 'APPLIED', 'INTERVIEWING', 'OFFER', 'ACCEPTED',
                                              'DECLINED', 'REJECTED', 'WITHDRAWN', 'GHOSTED')),
    salary_expectation  VARCHAR(200),
    salary_offer        VARCHAR(200),
    applied_date        DATE,
    closed_date         DATE,
    job_description     TEXT CHECK (char_length(job_description) <= 50000),
    archived            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_job_id_owner UNIQUE (id, owner_id)
);

CREATE INDEX idx_job_owner ON app.job (owner_id, archived);

CREATE TABLE app.round (
    id                BIGSERIAL PRIMARY KEY,
    owner_id          UUID NOT NULL,
    job_id            BIGINT NOT NULL,
    round_type        VARCHAR(30) NOT NULL
                          CHECK (round_type IN ('SCREENING', 'TECHNICAL', 'TAKE_HOME', 'BEHAVIORAL',
                                                'HIRING_MANAGER', 'ONSITE', 'FINAL', 'OTHER')),
    scheduled_at      TIMESTAMPTZ,
    outcome           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                          CHECK (outcome IN ('PENDING', 'PASSED', 'FAILED', 'CANCELLED')),
    felt_score        SMALLINT CHECK (felt_score BETWEEN 1 AND 5),
    difficulty_score  SMALLINT CHECK (difficulty_score BETWEEN 1 AND 5),
    questions_asked   TEXT CHECK (char_length(questions_asked) <= 20000),
    debrief           TEXT CHECK (char_length(debrief) <= 20000),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Composite, so a round can never be attached to another user's job.
    CONSTRAINT fk_round_job FOREIGN KEY (job_id, owner_id)
        REFERENCES app.job (id, owner_id) ON DELETE CASCADE
);

CREATE INDEX idx_round_owner_scheduled ON app.round (owner_id, scheduled_at);
CREATE INDEX idx_round_job ON app.round (job_id);

-- Logins are kept in the database so they survive restarts and a host that sleeps.
-- From spring-session-jdbc's schema-postgresql.sql; Flyway creates them.
CREATE TABLE app.spring_session (
    primary_id             CHAR(36) NOT NULL,
    session_id             CHAR(36) NOT NULL,
    creation_time          BIGINT NOT NULL,
    last_access_time       BIGINT NOT NULL,
    max_inactive_interval  INT NOT NULL,
    expiry_time            BIGINT NOT NULL,
    principal_name         VARCHAR(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX spring_session_ix1 ON app.spring_session (session_id);
CREATE INDEX spring_session_ix2 ON app.spring_session (expiry_time);
CREATE INDEX spring_session_ix3 ON app.spring_session (principal_name);

CREATE TABLE app.spring_session_attributes (
    session_primary_id  CHAR(36) NOT NULL,
    attribute_name      VARCHAR(200) NOT NULL,
    attribute_bytes     BYTEA NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id)
        REFERENCES app.spring_session (primary_id) ON DELETE CASCADE
);
