ALTER TABLE shop_products ADD COLUMN receiver_family_user_id BIGINT REFERENCES users(id);
CREATE INDEX idx_shop_products_wish_receiver ON shop_products(receiver_family_user_id, clover_price);
