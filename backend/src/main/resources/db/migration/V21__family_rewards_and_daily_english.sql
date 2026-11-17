alter table clover_ledgers add column note varchar(300);
alter table clover_ledgers add column given_by_user_id bigint;
alter table clover_ledgers add column given_by_name varchar(160);
alter table clover_ledgers add column request_key varchar(36);
alter table clover_ledgers add constraint uk_clover_award_request unique (given_by_user_id, request_key);

create table english_studies (
    id bigserial primary key,
    child_user_id bigint not null,
    receiver_family_user_id bigint not null,
    study_date date not null,
    sentence varchar(120) not null,
    meaning varchar(120) not null,
    status varchar(20) not null check (status in ('PENDING', 'APPROVED', 'REJECTED')),
    created_at timestamp with time zone not null,
    processed_at timestamp with time zone,
    constraint uk_english_child_day unique (child_user_id, study_date)
);
create index ix_english_receiver_status on english_studies(receiver_family_user_id, status, created_at);
