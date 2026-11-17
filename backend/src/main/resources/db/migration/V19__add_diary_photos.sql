create table diary_photos (
    diary_id bigint primary key references diaries(id) on delete cascade,
    image_data bytea not null
);
