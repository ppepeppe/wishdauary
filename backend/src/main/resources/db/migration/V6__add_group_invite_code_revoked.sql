alter table groups
    add column if not exists invite_code_revoked boolean not null default false;
