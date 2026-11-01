create table family_connection_requests (
    id bigserial primary key,
    family_user_id bigint not null references users(id),
    child_user_id bigint not null references users(id),
    status varchar(20) not null,
    created_at timestamp not null,
    processed_at timestamp,
    constraint uk_family_connection_pair unique (family_user_id, child_user_id),
    constraint ck_family_connection_status check (status in ('PENDING', 'APPROVED', 'REJECTED')),
    constraint ck_family_connection_different_users check (family_user_id <> child_user_id)
);
create index idx_family_connection_requests_child on family_connection_requests(child_user_id, created_at desc);
