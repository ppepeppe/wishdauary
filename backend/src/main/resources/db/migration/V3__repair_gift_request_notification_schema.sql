alter table gift_request_notifications
    add column if not exists clover_price integer not null default 0;

alter table gift_request_notifications
    add column if not exists status varchar(20) not null default 'PENDING';

alter table gift_request_notifications
    add column if not exists processed_by_family_user_id bigint;

alter table gift_request_notifications
    add column if not exists processed_at timestamp;

alter table gift_request_notifications
    add column if not exists purchase_price_won integer;

create index if not exists idx_gift_requests_receiver_created_at
    on gift_request_notifications (receiver_family_user_id, created_at desc);
