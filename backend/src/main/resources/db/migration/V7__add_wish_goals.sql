create table if not exists wish_goals (
    id bigserial primary key,
    child_user_id bigint not null,
    product_id bigint not null,
    created_at timestamp not null,
    constraint uk_wish_goal_child_user unique (child_user_id)
);
