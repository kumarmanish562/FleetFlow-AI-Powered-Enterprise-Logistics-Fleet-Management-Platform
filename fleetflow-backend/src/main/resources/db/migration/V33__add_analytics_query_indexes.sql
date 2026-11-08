-- Support date-window aggregates used by analytics reports.
CREATE INDEX idx_shipments_created_status
    ON shipments (created_at, status);

CREATE INDEX idx_deliveries_analytics_event_time_status
    ON deliveries ((COALESCE(delivered_at, updated_at, created_at)), status);

CREATE INDEX idx_billing_invoices_created_currency
    ON billing_invoices (created_at, currency)
    WHERE status <> 'CANCELLED';

CREATE INDEX idx_billing_payments_success_paid_at
    ON billing_payments (paid_at, currency)
    WHERE status = 'SUCCESS';

CREATE INDEX idx_billing_payments_created_status_currency
    ON billing_payments (created_at, status, currency);
