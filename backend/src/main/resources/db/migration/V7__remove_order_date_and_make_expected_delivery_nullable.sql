ALTER TABLE purchase_orders DROP COLUMN order_date;
ALTER TABLE purchase_orders ALTER COLUMN expected_delivery_date DROP NOT NULL;