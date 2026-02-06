-- ============================================================================
-- Flyway Migration V2: Authentication & Authorization Seed Data
-- Description: Seeds essential roles and privileges
-- ============================================================================

-- ============================================================================
-- SEED DATA: PRIVILEGES
-- ============================================================================
INSERT INTO privileges (name, resource, description) VALUES
-- Product privileges
('CREATE_PRODUCT', 'PRODUCT', 'Can create products'),
('READ_PRODUCT', 'PRODUCT', 'Can view products'),
('UPDATE_PRODUCT', 'PRODUCT', 'Can update products'),
('DELETE_PRODUCT', 'PRODUCT', 'Can delete products'),

-- Category privileges
('CREATE_CATEGORY', 'CATEGORY', 'Can create categories'),
('READ_CATEGORY', 'CATEGORY', 'Can view categories'),
('UPDATE_CATEGORY', 'CATEGORY', 'Can update categories'),
('DELETE_CATEGORY', 'CATEGORY', 'Can delete categories'),

-- Supplier privileges
('CREATE_SUPPLIER', 'SUPPLIER', 'Can create suppliers'),
('READ_SUPPLIER', 'SUPPLIER', 'Can view suppliers'),
('UPDATE_SUPPLIER', 'SUPPLIER', 'Can update suppliers'),
('DELETE_SUPPLIER', 'SUPPLIER', 'Can delete suppliers'),

-- Purchase Order privileges
('CREATE_PURCHASE_ORDER', 'PURCHASE_ORDER', 'Can create purchase orders'),
('READ_PURCHASE_ORDER', 'PURCHASE_ORDER', 'Can view purchase orders'),
('UPDATE_PURCHASE_ORDER', 'PURCHASE_ORDER', 'Can update purchase orders'),
('DELETE_PURCHASE_ORDER', 'PURCHASE_ORDER', 'Can delete purchase orders'),

-- Sales Order privileges
('CREATE_SALES_ORDER', 'SALES_ORDER', 'Can create sales orders'),
('READ_SALES_ORDER', 'SALES_ORDER', 'Can view sales orders'),
('UPDATE_SALES_ORDER', 'SALES_ORDER', 'Can update sales orders'),
('DELETE_SALES_ORDER', 'SALES_ORDER', 'Can delete sales orders'),

-- Customer privileges
('CREATE_CUSTOMER', 'CUSTOMER', 'Can create customers'),
('READ_CUSTOMER', 'CUSTOMER', 'Can view customers'),
('UPDATE_CUSTOMER', 'CUSTOMER', 'Can update customers'),
('DELETE_CUSTOMER', 'CUSTOMER', 'Can delete customers'),

-- Store privileges
('CREATE_STORE', 'STORE', 'Can create stores'),
('READ_STORE', 'STORE', 'Can view stores'),
('UPDATE_STORE', 'STORE', 'Can update stores'),
('DELETE_STORE', 'STORE', 'Can delete stores'),

-- User management privileges
('CREATE_USER', 'USER', 'Can create users'),
('READ_USER', 'USER', 'Can view users'),
('UPDATE_USER', 'USER', 'Can update users'),
('DELETE_USER', 'USER', 'Can delete users'),
('ASSIGN_ROLE', 'USER', 'Can assign roles to users'),

-- Report privileges
('VIEW_REPORTS', 'REPORT', 'Can view reports and analytics');

-- ============================================================================
-- SEED DATA: ROLES
-- ============================================================================
INSERT INTO roles (name, description) VALUES
('SUPER_ADMIN', 'System administrator with full access to all stores'),
('OWNER', 'Store owner with full access to their stores'),
('ADMIN', 'Store administrator with operational management access'),
('MANAGER', 'Store manager with inventory and order management access'),
('STAFF', 'Store staff with sales operations access'),
('CUSTOMER', 'Regular customer with product browsing and ordering access');

-- ============================================================================
-- SEED DATA: ROLE-PRIVILEGE ASSIGNMENTS
-- ============================================================================

-- SUPER_ADMIN: All privileges
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'SUPER_ADMIN';

-- OWNER: Full store management (except system-level user management)
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'OWNER'
AND p.name IN (
    'CREATE_PRODUCT', 'READ_PRODUCT', 'UPDATE_PRODUCT', 'DELETE_PRODUCT',
    'CREATE_CATEGORY', 'READ_CATEGORY', 'UPDATE_CATEGORY', 'DELETE_CATEGORY',
    'CREATE_SUPPLIER', 'READ_SUPPLIER', 'UPDATE_SUPPLIER', 'DELETE_SUPPLIER',
    'CREATE_PURCHASE_ORDER', 'READ_PURCHASE_ORDER', 'UPDATE_PURCHASE_ORDER', 'DELETE_PURCHASE_ORDER',
    'CREATE_SALES_ORDER', 'READ_SALES_ORDER', 'UPDATE_SALES_ORDER', 'DELETE_SALES_ORDER',
    'CREATE_CUSTOMER', 'READ_CUSTOMER', 'UPDATE_CUSTOMER', 'DELETE_CUSTOMER',
    'READ_STORE', 'UPDATE_STORE', 'DELETE_STORE',
    'CREATE_USER', 'READ_USER', 'UPDATE_USER', 'DELETE_USER', 'ASSIGN_ROLE',
    'VIEW_REPORTS'
);

-- ADMIN: Operational management (no role assignment)
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'ADMIN'
AND p.name IN (
    'CREATE_PRODUCT', 'READ_PRODUCT', 'UPDATE_PRODUCT', 'DELETE_PRODUCT',
    'CREATE_CATEGORY', 'READ_CATEGORY', 'UPDATE_CATEGORY', 'DELETE_CATEGORY',
    'CREATE_SUPPLIER', 'READ_SUPPLIER', 'UPDATE_SUPPLIER', 'DELETE_SUPPLIER',
    'CREATE_PURCHASE_ORDER', 'READ_PURCHASE_ORDER', 'UPDATE_PURCHASE_ORDER', 'DELETE_PURCHASE_ORDER',
    'CREATE_SALES_ORDER', 'READ_SALES_ORDER', 'UPDATE_SALES_ORDER', 'DELETE_SALES_ORDER',
    'CREATE_CUSTOMER', 'READ_CUSTOMER', 'UPDATE_CUSTOMER', 'DELETE_CUSTOMER',
    'VIEW_REPORTS'
);

-- MANAGER: Inventory and order management
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'MANAGER'
AND p.name IN (
    'CREATE_PRODUCT', 'READ_PRODUCT', 'UPDATE_PRODUCT',
    'READ_CATEGORY',
    'READ_SUPPLIER',
    'CREATE_PURCHASE_ORDER', 'READ_PURCHASE_ORDER', 'UPDATE_PURCHASE_ORDER',
    'CREATE_SALES_ORDER', 'READ_SALES_ORDER', 'UPDATE_SALES_ORDER',
    'CREATE_CUSTOMER', 'READ_CUSTOMER', 'UPDATE_CUSTOMER'
);

-- STAFF: Sales operations only
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'STAFF'
AND p.name IN (
    'READ_PRODUCT',
    'READ_CATEGORY',
    'CREATE_SALES_ORDER', 'READ_SALES_ORDER',
    'READ_CUSTOMER'
);

-- CUSTOMER: Public browsing and ordering
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r, privileges p
WHERE r.name = 'CUSTOMER'
AND p.name IN (
    'READ_PRODUCT',
    'READ_CATEGORY',
    'CREATE_SALES_ORDER', 'READ_SALES_ORDER'
);

-- ============================================================================
-- END OF MIGRATION V2
-- ============================================================================