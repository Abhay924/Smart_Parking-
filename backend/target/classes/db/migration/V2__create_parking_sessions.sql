CREATE TABLE parking_sessions (
    id BIGSERIAL PRIMARY KEY,

    vehicle_id BIGINT NOT NULL,

    entry_time TIMESTAMPTZ NOT NULL,

    exit_time TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_parking_session_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
);

CREATE INDEX idx_parking_session_vehicle
    ON parking_sessions(vehicle_id);

CREATE INDEX idx_parking_session_status
    ON parking_sessions(status);