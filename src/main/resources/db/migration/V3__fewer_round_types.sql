-- Behavioral and onsite rounds go: they describe a format more than a step. Existing ones become "other".
UPDATE app.round SET round_type = 'OTHER' WHERE round_type IN ('BEHAVIORAL', 'ONSITE');

ALTER TABLE app.round DROP CONSTRAINT round_round_type_check;
ALTER TABLE app.round ADD CONSTRAINT round_round_type_check
    CHECK (round_type IN ('SCREENING', 'TECHNICAL', 'TAKE_HOME', 'HIRING_MANAGER', 'FINAL', 'OTHER'));
