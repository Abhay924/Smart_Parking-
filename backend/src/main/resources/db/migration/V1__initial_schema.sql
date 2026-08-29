-- ============================================================
-- SMART PARKING SYSTEM
-- V1 - Initial Database Schema
-- PostgreSQL
-- ============================================================


-- ============================================================
-- 1. VEHICLES
-- Stores unique vehicles identified by their registration plate
-- ============================================================

CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,

    plate_number VARCHAR(20) NOT NULL,

    vehicle_type VARCHAR(20) NOT NULL DEFAULT 'CAR',

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_vehicles_plate_number
        UNIQUE (plate_number),

    CONSTRAINT chk_vehicles_plate_number
        CHECK (plate_number = UPPER(plate_number)),

    CONSTRAINT chk_vehicles_type
        CHECK (vehicle_type IN ('CAR'))
);


CREATE INDEX idx_vehicles_plate_number
    ON vehicles (plate_number);


-- ============================================================
-- 2. PARKING RATES
-- Configurable parking pricing
-- ============================================================

CREATE TABLE parking_rates (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    vehicle_type VARCHAR(20) NOT NULL DEFAULT 'CAR',

    rate_per_hour NUMERIC(10,2) NOT NULL,

    minimum_charge NUMERIC(10,2) NOT NULL DEFAULT 0,

    grace_period_minutes INTEGER NOT NULL DEFAULT 0,

    maximum_daily_charge NUMERIC(10,2),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_rates_vehicle_type
        CHECK (vehicle_type IN ('CAR')),

    CONSTRAINT chk_rates_rate
        CHECK (rate_per_hour >= 0),

    CONSTRAINT chk_rates_minimum
        CHECK (minimum_charge >= 0),

    CONSTRAINT chk_rates_grace
        CHECK (grace_period_minutes >= 0),

    CONSTRAINT chk_rates_daily
        CHECK (
            maximum_daily_charge IS NULL
            OR maximum_daily_charge >= 0
        )
);


-- ============================================================
-- 3. PARKING SESSIONS
-- Entry → Parking → Exit
-- ============================================================

CREATE TABLE parking_sessions (
    id BIGSERIAL PRIMARY KEY,

    vehicle_id BIGINT NOT NULL,

    entry_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    exit_time TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    entry_plate_confidence NUMERIC(5,4),

    exit_plate_confidence NUMERIC(5,4),

    entry_image_path TEXT,

    exit_image_path TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_parking_sessions_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id),

    CONSTRAINT chk_session_status
        CHECK (
            status IN (
                'ACTIVE',
                'COMPLETED',
                'CANCELLED',
                'MANUAL_REVIEW'
            )
        ),

    CONSTRAINT chk_session_exit_time
        CHECK (
            exit_time IS NULL
            OR exit_time >= entry_time
        ),

    CONSTRAINT chk_entry_confidence
        CHECK (
            entry_plate_confidence IS NULL
            OR (
                entry_plate_confidence >= 0
                AND entry_plate_confidence <= 1
            )
        ),

    CONSTRAINT chk_exit_confidence
        CHECK (
            exit_plate_confidence IS NULL
            OR (
                exit_plate_confidence >= 0
                AND exit_plate_confidence <= 1
            )
        )
);


CREATE INDEX idx_parking_sessions_vehicle
    ON parking_sessions (vehicle_id);

CREATE INDEX idx_parking_sessions_status
    ON parking_sessions (status);

CREATE INDEX idx_parking_sessions_entry_time
    ON parking_sessions (entry_time);

CREATE INDEX idx_parking_sessions_exit_time
    ON parking_sessions (exit_time);


-- Only one active parking session per vehicle
CREATE UNIQUE INDEX uk_active_parking_session
    ON parking_sessions (vehicle_id)
    WHERE status = 'ACTIVE';


-- ============================================================
-- 4. BILLS
-- Generated when a vehicle exits
-- ============================================================

CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,

    bill_number VARCHAR(50) NOT NULL,

    parking_session_id BIGINT NOT NULL,

    rate_id BIGINT,

    duration_minutes BIGINT NOT NULL,

    base_amount NUMERIC(10,2) NOT NULL,

    tax_amount NUMERIC(10,2) NOT NULL DEFAULT 0,

    discount_amount NUMERIC(10,2) NOT NULL DEFAULT 0,

    total_amount NUMERIC(10,2) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',

    generated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    paid_at TIMESTAMPTZ,

    CONSTRAINT uk_bills_number
        UNIQUE (bill_number),

    CONSTRAINT uk_bills_session
        UNIQUE (parking_session_id),

    CONSTRAINT fk_bills_session
        FOREIGN KEY (parking_session_id)
        REFERENCES parking_sessions(id),

    CONSTRAINT fk_bills_rate
        FOREIGN KEY (rate_id)
        REFERENCES parking_rates(id),

    CONSTRAINT chk_bill_duration
        CHECK (duration_minutes >= 0),

    CONSTRAINT chk_bill_base
        CHECK (base_amount >= 0),

    CONSTRAINT chk_bill_tax
        CHECK (tax_amount >= 0),

    CONSTRAINT chk_bill_discount
        CHECK (discount_amount >= 0),

    CONSTRAINT chk_bill_total
        CHECK (total_amount >= 0),

    CONSTRAINT chk_bill_status
        CHECK (
            status IN (
                'UNPAID',
                'PAYMENT_PENDING',
                'PAID',
                'CANCELLED',
                'REFUNDED'
            )
        )
);


CREATE INDEX idx_bills_status
    ON bills (status);

CREATE INDEX idx_bills_generated_at
    ON bills (generated_at);


-- ============================================================
-- 5. PAYMENTS
-- Payment gateway transaction records
-- ============================================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,

    bill_id BIGINT NOT NULL,

    provider VARCHAR(50) NOT NULL,

    provider_payment_id VARCHAR(150),

    provider_order_id VARCHAR(150),

    amount NUMERIC(10,2) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    payment_method VARCHAR(30),

    qr_reference VARCHAR(150),

    paid_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills(id),

    CONSTRAINT chk_payment_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payment_status
        CHECK (
            status IN (
                'CREATED',
                'PENDING',
                'SUCCESS',
                'FAILED',
                'CANCELLED',
                'REFUNDED'
            )
        )
);


CREATE INDEX idx_payments_bill
    ON payments (bill_id);

CREATE INDEX idx_payments_status
    ON payments (status);

CREATE UNIQUE INDEX uk_provider_payment
    ON payments (provider, provider_payment_id)
    WHERE provider_payment_id IS NOT NULL;


-- ============================================================
-- 6. ANPR EVENTS
-- Every important plate-recognition event is recorded
-- Useful for debugging, accuracy measurement and audit
-- ============================================================

CREATE TABLE anpr_events (
    id BIGSERIAL PRIMARY KEY,

    plate_number VARCHAR(20),

    confidence NUMERIC(5,4),

    camera_id VARCHAR(100) NOT NULL,

    direction VARCHAR(10) NOT NULL,

    image_path TEXT,

    recognized_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    processing_time_ms INTEGER,

    is_accepted BOOLEAN NOT NULL DEFAULT FALSE,

    rejection_reason VARCHAR(255),

    CONSTRAINT chk_anpr_confidence
        CHECK (
            confidence IS NULL
            OR (
                confidence >= 0
                AND confidence <= 1
            )
        ),

    CONSTRAINT chk_anpr_direction
        CHECK (
            direction IN ('ENTRY', 'EXIT')
        )
);


CREATE INDEX idx_anpr_plate
    ON anpr_events (plate_number);

CREATE INDEX idx_anpr_camera
    ON anpr_events (camera_id);

CREATE INDEX idx_anpr_recognized_at
    ON anpr_events (recognized_at);


-- ============================================================
-- 7. GATE EVENTS
-- Every gate command/status change is logged
-- ============================================================

CREATE TABLE gate_events (
    id BIGSERIAL PRIMARY KEY,

    gate_id VARCHAR(100) NOT NULL,

    direction VARCHAR(10) NOT NULL,

    action VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    parking_session_id BIGINT,

    bill_id BIGINT,

    correlation_id UUID NOT NULL,

    reason VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gate_session
        FOREIGN KEY (parking_session_id)
        REFERENCES parking_sessions(id),

    CONSTRAINT fk_gate_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills(id),

    CONSTRAINT chk_gate_direction
        CHECK (
            direction IN ('ENTRY', 'EXIT')
        ),

    CONSTRAINT chk_gate_action
        CHECK (
            action IN (
                'OPEN',
                'CLOSE',
                'STATUS_CHECK',
                'EMERGENCY_OPEN',
                'MANUAL_OPEN'
            )
        )
);


CREATE INDEX idx_gate_events_gate
    ON gate_events (gate_id);

CREATE INDEX idx_gate_events_created
    ON gate_events (created_at);

CREATE INDEX idx_gate_events_correlation
    ON gate_events (correlation_id);


-- ============================================================
-- 8. AUDIT LOGS
-- Security and administrative audit trail
-- ============================================================

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,

    actor_type VARCHAR(30) NOT NULL,

    actor_id VARCHAR(100),

    action VARCHAR(100) NOT NULL,

    entity_type VARCHAR(100),

    entity_id VARCHAR(100),

    ip_address INET,

    user_agent TEXT,

    details JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_audit_logs_actor
    ON audit_logs (actor_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs (action);

CREATE INDEX idx_audit_logs_created
    ON audit_logs (created_at);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs (entity_type, entity_id);