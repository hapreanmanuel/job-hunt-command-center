-- Dated notes per job replace "how it started", and the wishlist status goes: only submitted
-- applications are tracked.

CREATE TABLE app.note (
    id          BIGSERIAL PRIMARY KEY,
    owner_id    UUID NOT NULL,
    job_id      BIGINT NOT NULL,
    note_text   TEXT NOT NULL CHECK (char_length(note_text) <= 20000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Composite, so a note can never be attached to another user's job.
    CONSTRAINT fk_note_job FOREIGN KEY (job_id, owner_id)
        REFERENCES app.job (id, owner_id) ON DELETE CASCADE
);

CREATE INDEX idx_note_job ON app.note (job_id, owner_id);
CREATE INDEX idx_note_owner ON app.note (owner_id);

-- Keep what "how it started" said, as a note dated when the job was added.
INSERT INTO app.note (owner_id, job_id, note_text, created_at, updated_at)
SELECT owner_id, id, text, created_at, created_at
FROM (SELECT owner_id, id, created_at,
             concat_ws(E'\n',
                 CASE source
                     WHEN 'FOUND_MYSELF' THEN 'I found it and applied.'
                     WHEN 'RECRUITER' THEN 'A recruiter contacted me.'
                     WHEN 'REFERRAL' THEN 'Someone referred me.'
                 END,
                 NULLIF(trim(source_note), '')) AS text
      FROM app.job) started
WHERE text <> '';

ALTER TABLE app.job DROP COLUMN source, DROP COLUMN source_note;

UPDATE app.job
SET status = 'APPLIED', applied_date = coalesce(applied_date, created_at::date)
WHERE status = 'WISHLIST';

ALTER TABLE app.job DROP CONSTRAINT job_status_check;
ALTER TABLE app.job ADD CONSTRAINT job_status_check
    CHECK (status IN ('APPLIED', 'INTERVIEWING', 'OFFER', 'ACCEPTED',
                      'DECLINED', 'REJECTED', 'WITHDRAWN', 'GHOSTED'));
ALTER TABLE app.job ALTER COLUMN status SET DEFAULT 'APPLIED';
