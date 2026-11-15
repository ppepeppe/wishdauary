alter table users
    add column user_role varchar(20) not null default 'USER';

alter table users
    add constraint users_user_role_check check (user_role in ('ADMIN', 'USER'));
