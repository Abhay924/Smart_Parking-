
ALTER TABLE bills
    ADD COLUMN IF NOT EXISTS duration_minutes BIGINT;

ALTER TABLE bills
    ADD COLUMN IF NOT EXISTS grace_minutes INTEGER;

ALTER TABLE bills
    ADD COLUMN IF NOT EXISTS amount NUMERIC(10,2);

ALTER TABLE bills
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

ALTER TABLE bills
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

-- Existing rows ko safe defaults do.
UPDATE bills
SET duration_minutes = COALESCE(duration_minutes, 0),
    grace_minutes = COALESCE(grace_minutes, 0),
    amount = COALESCE(amount, 0),
    status = COALESCE(status, 'UNPAID'),
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP);

-- Required constraints
ALTER TABLE bills
    ALTER COLUMN duration_minutes SET NOT NULL;

ALTER TABLE bills
    ALTER COLUMN grace_minutes SET NOT NULL;

ALTER TABLE bills
    ALTER COLUMN amount SET NOT NULL;

ALTER TABLE bills
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE bills
    ALTER COLUMN created_at SET NOT NULL;