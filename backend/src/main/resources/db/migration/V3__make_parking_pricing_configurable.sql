-- ============================================================
-- V3 - Make Parking Pricing Configurable
-- ============================================================

ALTER TABLE parking_rates
    ADD COLUMN billing_mode VARCHAR(20) NOT NULL DEFAULT 'PER_HOUR';

ALTER TABLE parking_rates
    ADD COLUMN rate_per_unit NUMERIC(10,2);

-- Existing hourly rates become rate_per_unit
UPDATE parking_rates
SET rate_per_unit = rate_per_hour;

ALTER TABLE parking_rates
    ALTER COLUMN rate_per_unit SET NOT NULL;

ALTER TABLE parking_rates
    ADD CONSTRAINT chk_parking_rate_billing_mode
    CHECK (
        billing_mode IN (
            'PER_MINUTE',
            'PER_HOUR',
            'FLAT_RATE'
        )
    );

ALTER TABLE parking_rates
    ADD CONSTRAINT chk_parking_rate_per_unit
    CHECK (rate_per_unit >= 0);