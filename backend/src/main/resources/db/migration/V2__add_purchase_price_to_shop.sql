alter table shop_products
    add column if not exists purchase_price_won integer;

alter table gift_request_notifications
    add column if not exists purchase_price_won integer;
