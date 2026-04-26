-- ============================================================================
-- V5: Add phone and address to users, update image_urls to JSONB in products
-- ============================================================================

-- Add phone and address to users (nullable)
ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL;
ALTER TABLE users ADD COLUMN address TEXT NULL;

-- Replace image_urls TEXT with JSONB in products
ALTER TABLE products DROP COLUMN IF EXISTS image_urls;
ALTER TABLE products ADD COLUMN image_urls JSONB NULL DEFAULT '[]';