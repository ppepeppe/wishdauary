alter table shop_products
    add column if not exists child_user_id bigint;

alter table shop_products
    add column if not exists image_url varchar(500);

alter table shop_products
    add column if not exists product_url varchar(1000);

alter table shop_products
    add column if not exists clover_price integer not null default 0;

alter table shop_products
    add column if not exists purchase_price_won integer;

alter table shop_products
    add column if not exists source varchar(20) not null default 'PRESET';

create index if not exists idx_shop_products_child_source
    on shop_products (child_user_id, source);
