-- ============================================================================
-- Flyway Migration V1: Authentication & Authorization Schema
-- Description: Creates tables for user management, roles, and privileges
-- ============================================================================

-- ============================================================================
-- PRIVILEGES TABLE
-- ============================================================================
CREATE TABLE privileges (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE, 
    resource VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- ROLES TABLE
-- ============================================================================
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- ROLE-PRIVILEGES (Many-to-Many)
-- ============================================================================
CREATE TABLE role_privileges ( 
    role_id BIGINT NOT NULL,
    privilege_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, privilege_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (privilege_id) REFERENCES privileges(id) ON DELETE CASCADE
);

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- USER-ROLES (Many-to-Many with Store Scope)
-- ============================================================================
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id BIGINT NOT NULL,
    store_id UUID NULL,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Global roles (SUPER_ADMIN, CUSTOMER)
CREATE UNIQUE INDEX ux_user_roles_global
ON user_roles (user_id, role_id)
WHERE store_id IS NULL;

-- Store-scoped roles (OWNER, ADMIN, MANAGER, STAFF)
CREATE UNIQUE INDEX ux_user_roles_store
ON user_roles (user_id, role_id, store_id)
WHERE store_id IS NOT NULL;

-- ============================================================================
-- INDEXES 
-- ============================================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_user_roles_store_id ON user_roles(store_id);

-- ============================================================================
-- END OF MIGRATION V1
-- ============================================================================