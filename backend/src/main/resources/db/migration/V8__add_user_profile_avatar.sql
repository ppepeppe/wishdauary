alter table users
    add column if not exists avatar_preset varchar(30) not null default 'peach';

alter table users
    add column if not exists avatar_image text;
