-- Family-only rollout: existing adult accounts can manage the shared catalog.
update users set user_role = 'ADMIN' where app_role = 'FAMILY';
alter table shop_products add column deleted boolean not null default false;
alter table shop_products add column created_by_user_id bigint;
