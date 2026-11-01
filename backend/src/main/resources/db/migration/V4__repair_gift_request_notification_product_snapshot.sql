alter table gift_request_notifications
    add column if not exists product_id bigint;

alter table gift_request_notifications
    add column if not exists product_title varchar(120);

alter table gift_request_notifications
    add column if not exists product_image_url varchar(500);

alter table gift_request_notifications
    add column if not exists product_url varchar(1000);

update gift_request_notifications
set product_id = 0
where product_id is null;

update gift_request_notifications
set product_title = '이름 없는 선물'
where product_title is null;

alter table gift_request_notifications
    alter column product_id set not null;

alter table gift_request_notifications
    alter column product_title set not null;
