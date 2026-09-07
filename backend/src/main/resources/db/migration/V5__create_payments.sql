
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'payments'
    ) THEN
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
                FOREIGN KEY (bill_id) REFERENCES bills(id),

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
            ON payments(bill_id);

        CREATE INDEX idx_payments_status
            ON payments(status);

        CREATE UNIQUE INDEX uk_provider_payment
            ON payments(provider, provider_payment_id)
            WHERE provider_payment_id IS NOT NULL;
    END IF;
END $$;