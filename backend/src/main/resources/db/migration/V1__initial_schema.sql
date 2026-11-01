create table if not exists users (
    id bigserial primary key,
    email varchar(255) not null unique,
    nickname varchar(255) not null,
    app_role varchar(20) not null,
    password_hash varchar(255) not null
);

create table if not exists groups (
    id bigserial primary key,
    invite_code varchar(50) not null unique
);

create table if not exists group_members (
    id bigserial primary key,
    group_id bigint not null,
    user_id bigint not null,
    member_role varchar(20) not null,
    family_relation varchar(30),
    relation_label varchar(100),
    constraint uk_group_member_group_user unique (group_id, user_id)
);

create table if not exists user_stats (
    user_id bigint primary key,
    current_streak integer not null,
    max_streak integer not null,
    last_written_date date
);

create table if not exists diaries (
    id bigserial primary key,
    author_id bigint not null,
    content text not null,
    emotion varchar(20),
    visibility_scope varchar(20) not null,
    written_date date not null,
    created_at timestamp not null
);

create table if not exists heart_reactions (
    id bigserial primary key,
    diary_id bigint not null,
    family_user_id bigint not null,
    created_at timestamp not null,
    constraint uk_heart_reaction_diary_family_user unique (diary_id, family_user_id)
);

create table if not exists diary_comments (
    id bigserial primary key,
    diary_id bigint not null,
    author_id bigint not null,
    content text not null,
    created_at timestamp not null
);

create table if not exists clover_ledgers (
    id bigserial primary key,
    user_id bigint not null,
    amount integer not null,
    reason varchar(50) not null,
    ref_id bigint,
    created_at timestamp not null,
    constraint uk_clover_ledger_user_reason_ref unique (user_id, reason, ref_id)
);

create table if not exists shop_products (
    id bigserial primary key,
    child_user_id bigint,
    title varchar(120) not null,
    description varchar(500) not null,
    image_url varchar(500),
    product_url varchar(1000),
    clover_price integer not null,
    source varchar(20) not null,
    created_at timestamp not null
);

create table if not exists gift_request_notifications (
    id bigserial primary key,
    product_id bigint not null,
    product_title varchar(120) not null,
    product_image_url varchar(500),
    product_url varchar(1000),
    clover_price integer not null default 0,
    requester_child_user_id bigint not null,
    requester_child_nickname varchar(100) not null,
    receiver_family_user_id bigint not null,
    receiver_relation_label varchar(100),
    message varchar(500) not null,
    status varchar(20) not null default 'PENDING',
    processed_by_family_user_id bigint,
    processed_at timestamp,
    created_at timestamp not null
);

create index if not exists idx_diaries_author_written_date on diaries (author_id, written_date);
create index if not exists idx_diaries_author_visibility on diaries (author_id, visibility_scope);
create index if not exists idx_diary_comments_diary_created_at on diary_comments (diary_id, created_at);
create index if not exists idx_gift_requests_receiver_created_at on gift_request_notifications (receiver_family_user_id, created_at desc);
create index if not exists idx_shop_products_child_source on shop_products (child_user_id, source);
