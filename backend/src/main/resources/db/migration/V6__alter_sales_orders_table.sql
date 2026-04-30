-- ============================================================================
-- V6: Add phone and recipient name column to sales_orders
-- ============================================================================
ALTER TABLE sales_orders ADD COLUMN phone VARCHAR(20) NULL;
ALTER TABLE sales_orders ADD COLUMN recipient_name VARCHAR(100) NULL;