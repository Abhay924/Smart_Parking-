
INSERT INTO parking_rates (
    name,
    vehicle_type,
    rate_per_hour,
    minimum_charge,
    grace_period_minutes,
    maximum_daily_charge
)
SELECT
    'Standard Car Rate',
    'CAR',
    30.00,
    20.00,
    15,
    300.00
WHERE NOT EXISTS (
    SELECT 1
    FROM parking_rates
    WHERE name = 'Standard Car Rate'
);