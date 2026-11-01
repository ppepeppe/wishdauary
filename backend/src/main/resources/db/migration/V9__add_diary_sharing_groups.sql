create table diary_sharing_groups (
    id bigserial primary key,
    owner_id bigint not null references users(id),
    name varchar(30) not null,
    slot integer,
    constraint ck_diary_sharing_group_slot check (slot between 1 and 5),
    constraint uk_diary_sharing_group_owner_slot unique (owner_id, slot)
);

create table diary_sharing_group_members (
    sharing_group_id bigint not null references diary_sharing_groups(id),
    user_id bigint not null references users(id),
    primary key (sharing_group_id, user_id)
);

create table diary_audience_groups (
    diary_id bigint not null references diaries(id) on delete cascade,
    sharing_group_id bigint not null references diary_sharing_groups(id),
    primary key (diary_id, sharing_group_id)
);

create index idx_sharing_group_member_user on diary_sharing_group_members(user_id, sharing_group_id);
create index idx_diary_audience_group on diary_audience_groups(sharing_group_id, diary_id);
create index idx_diaries_visibility_created on diaries(visibility_scope, created_at desc);
