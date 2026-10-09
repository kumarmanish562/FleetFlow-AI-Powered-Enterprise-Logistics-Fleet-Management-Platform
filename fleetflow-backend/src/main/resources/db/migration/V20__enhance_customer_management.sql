-- Add customer profile fields.
ALTER TABLE customers
    ADD COLUMN customer_code VARCHAR(40),
    ADD COLUMN company_name VARCHAR(200),
    ADD COLUMN first_name VARCHAR(100),
    ADD COLUMN last_name VARCHAR(100),
    ADD COLUMN tax_identifier VARCHAR(100);

-- Add structured billing address fields.
ALTER TABLE customers
    ADD COLUMN billing_address_line1 VARCHAR(200),
    ADD COLUMN billing_address_line2 VARCHAR(200),
    ADD COLUMN billing_city VARCHAR(100),
    ADD COLUMN billing_state VARCHAR(100),
    ADD COLUMN billing_postal_code VARCHAR(20),
    ADD COLUMN billing_country VARCHAR(100);

-- Add structured shipping address fields.
ALTER TABLE customers
    ADD COLUMN shipping_address_line1 VARCHAR(200),
    ADD COLUMN shipping_address_line2 VARCHAR(200),
    ADD COLUMN shipping_city VARCHAR(100),
    ADD COLUMN shipping_state VARCHAR(100),
    ADD COLUMN shipping_postal_code VARCHAR(20),
    ADD COLUMN shipping_country VARCHAR(100);

-- Backfill the new name fields.
UPDATE customers
SET first_name = COALESCE(
        NULLIF(SPLIT_PART(TRIM(name), ' ', 1), ''),
        'Unknown'
    ),
    last_name = COALESCE(
        NULLIF(
            SUBSTRING(
                TRIM(name)
                FROM POSITION(' ' IN TRIM(name)) + 1
            ),
            ''
        ),
        'Unknown'
    );

-- Generate deterministic codes for existing records.
UPDATE customers
SET customer_code =
    'CUS-' ||
    UPPER(SUBSTRING(REPLACE(id::TEXT, '-', '') FROM 1 FOR 12));

-- Preserve legacy business names.
UPDATE customers
SET company_name = name
WHERE customer_type = 'BUSINESS'
  AND NULLIF(TRIM(company_name), '') IS NULL;

-- Map the legacy status value to the selected lifecycle.
ALTER TABLE customers
    DROP CONSTRAINT IF EXISTS chk_customer_status;

UPDATE customers
SET status = 'SUSPENDED'
WHERE status = 'BLOCKED';

-- Add required constraints only after data has been backfilled.
ALTER TABLE customers
    ALTER COLUMN customer_code SET NOT NULL,
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE customers
    ADD CONSTRAINT uk_customers_customer_code
    UNIQUE (customer_code);

ALTER TABLE customers
    ADD CONSTRAINT chk_customer_status
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'));

-- Case-insensitive email uniqueness.
CREATE UNIQUE INDEX uk_customers_email_lower
    ON customers (LOWER(email))
    WHERE email IS NOT NULL;

-- Search and filter indexes.
CREATE INDEX IF NOT EXISTS idx_customers_type
    ON customers (customer_type);

CREATE INDEX IF NOT EXISTS idx_customers_name
    ON customers (last_name, first_name);

CREATE INDEX IF NOT EXISTS idx_customers_status
    ON customers (status);