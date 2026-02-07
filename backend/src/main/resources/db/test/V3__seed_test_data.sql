-- ============================================================================
-- TEST ONLY: Seed Data for Automated Testing
-- ============================================================================
-- Description: Creates test users and role assignments for repository tests
-- WARNING: This file should ONLY be used in test environment!
-- ============================================================================

-- ============================================================================
-- TEST USER 1: SUPER_ADMIN
-- ============================================================================
INSERT INTO users (id, email, password, full_name, provider, email_verified, enabled)
VALUES (
    '00000000-0000-0000-0000-000000000001',  -- Fixed UUID for predictable tests
    'test.superadmin@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- "password123"
    'Test Super Admin',
    'LOCAL',
    true,
    true
);

-- Assign SUPER_ADMIN role (global)
INSERT INTO user_roles (user_id, role_id, store_id)
SELECT 
    '00000000-0000-0000-0000-000000000001'::uuid,
    r.id,
    NULL
FROM roles r
WHERE r.name = 'SUPER_ADMIN';

-- ============================================================================
-- TEST USER 2: CUSTOMER
-- ============================================================================
INSERT INTO users (id, email, password, full_name, provider, email_verified, enabled)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'test.customer@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Test Customer',
    'LOCAL',
    false,  -- Not verified
    true
);

-- Assign CUSTOMER role (global)
INSERT INTO user_roles (user_id, role_id, store_id)
SELECT 
    '00000000-0000-0000-0000-000000000002'::uuid,
    r.id,
    NULL
FROM roles r
WHERE r.name = 'CUSTOMER';

-- ============================================================================
-- TEST USER 3: OWNER (for future store tests)
-- ============================================================================
INSERT INTO users (id, email, password, full_name, provider, email_verified, enabled)
VALUES (
    '00000000-0000-0000-0000-000000000003',
    'test.owner@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Test Owner',
    'LOCAL',
    true,
    true
);

-- Assign OWNER role (global, no store yet)
INSERT INTO user_roles (user_id, role_id, store_id)
SELECT 
    '00000000-0000-0000-0000-000000000003'::uuid,
    r.id,
    NULL
FROM roles r
WHERE r.name = 'OWNER';

-- ============================================================================
-- TEST USER 4: Disabled user (for testing enabled/disabled logic)
-- ============================================================================
INSERT INTO users (id, email, password, full_name, provider, email_verified, enabled)
VALUES (
    '00000000-0000-0000-0000-000000000004',
    'test.disabled@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Test Disabled User',
    'LOCAL',
    true,
    false  -- Disabled!
);

-- Assign CUSTOMER role
INSERT INTO user_roles (user_id, role_id, store_id)
SELECT 
    '00000000-0000-0000-0000-000000000004'::uuid,
    r.id,
    NULL
FROM roles r
WHERE r.name = 'CUSTOMER';

-- ============================================================================
-- TEST USER 5: OAuth user (for OAuth testing)
-- ============================================================================
INSERT INTO users (id, email, password, full_name, provider, provider_id, email_verified, enabled)
VALUES (
    '00000000-0000-0000-0000-000000000005',
    'test.oauth@test.com',
    '',  -- No password for OAuth users
    'Test OAuth User',
    'GOOGLE',
    'google-id-12345',
    true,
    true
);

-- Assign CUSTOMER role
INSERT INTO user_roles (user_id, role_id, store_id)
SELECT 
    '00000000-0000-0000-0000-000000000005'::uuid,
    r.id,
    NULL
FROM roles r
WHERE r.name = 'CUSTOMER';

-- ============================================================================
-- END OF TEST SEED DATA
-- ============================================================================