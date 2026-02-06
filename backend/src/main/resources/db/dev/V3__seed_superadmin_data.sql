-- ============================================================================
-- Flyway Migration V3: SUPER ADMIN SEED DATA
-- ============================================================================

-- SEED DATA: DEFAULT SUPER_ADMIN USER
INSERT INTO users (id, email, password, full_name, provider, email_verified, enabled)
VALUES (
    gen_random_uuid(),
    'superadmin@retailhub.com',
    '$2a$12$bU07YKDn6HdAo6pgXqK/kO2xM9gdMOumCmVfnf1O.3c1JG9NfKHX.',  -- PASS: superretailhubadmin
    'System Administrator',
    'LOCAL',
    true,  -- Email already verified for initial admin
    true   -- Account enabled
);

-- Assign SUPER_ADMIN role (no store assignment)
INSERT INTO user_roles (user_id, role_id, store_id)
SELECT u.id, r.id, NULL
FROM users u, roles r
WHERE u.email = 'superadmin@retailhub.com'
  AND r.name = 'SUPER_ADMIN';

  
-- ============================================================================
-- END OF MIGRATION V3
-- ============================================================================