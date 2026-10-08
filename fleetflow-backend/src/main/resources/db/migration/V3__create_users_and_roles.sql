-- ============================================================
-- FleetFlow - Identity & RBAC
-- Roles, Users and User Roles
-- ============================================================

-- ============================================================
-- ROLES
-- ============================================================

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_roles_name
        UNIQUE (name),

    CONSTRAINT chk_roles_name
        CHECK (
            name IN (
                'SUPER_ADMIN',
                'ADMIN',
                'FLEET_MANAGER',
                'DISPATCHER',
                'DRIVER',
                'CUSTOMER',
                'FINANCE_MANAGER'
            )
        )
);

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'LOCKED',
                'SUSPENDED'
            )
        )
);

-- ============================================================
-- USER ROLES
-- ============================================================

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT pk_user_roles
        PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_users_status
    ON users(status);

CREATE INDEX idx_user_roles_role
    ON user_roles(role_id);

---- ============================================================
---- SEED DEFAULT ROLES
---- ============================================================
--
--INSERT INTO roles (
--    id,
--    name,
--    description,
--    created_at,
--    updated_at
--)
--VALUES
--    (
--        gen_random_uuid(),
--        'SUPER_ADMIN',
--        'Full platform administration access',
--        NOW(),
--        NOW()
--    ),
--    (
--        gen_random_uuid(),
--        'ADMIN',
--        'Organization administration access',
--        NOW(),
--        NOW()
--    ),
--    (
--        gen_random_uuid(),
--        'FLEET_MANAGER',
--        'Fleet and vehicle management access',
--        NOW(),
--        NOW()
--    ),
--    (
--        gen_random_uuid(),
--        'DISPATCHER',
--        'Shipment and dispatch management access',
--        NOW(),
--        NOW()
--    ),
--    (
--        gen_random_uuid(),
--        'DRIVER',
--        'Driver and assigned delivery access',
--        NOW(),
--        NOW()
--    ),
--    (
--        gen_random_uuid(),
--        'CUSTOMER',
--        'Customer shipment and delivery access',
--        NOW(),
--        NOW()
--    ),
--    (
--        gen_random_uuid(),
--        'FINANCE_MANAGER',
--        'Billing, invoice and financial management access',
--        NOW(),
--        NOW()
--    );