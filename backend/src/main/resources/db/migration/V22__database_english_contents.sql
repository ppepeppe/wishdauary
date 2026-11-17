create table english_contents (
    id bigserial primary key,
    sentence varchar(120) not null check (length(trim(sentence)) > 0),
    meaning varchar(120) not null check (length(trim(meaning)) > 0),
    version bigint not null default 0,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
);
create table english_daily_contents (
    study_date date primary key,
    content_id bigint not null references english_contents(id)
);

-- Keep the original order so the initial 30-row rotation stays identical.
insert into english_contents (sentence, meaning) values
    ('Hello!', '안녕!'),
    ('Good morning!', '좋은 아침이야!'),
    ('Thank you.', '고마워요.'),
    ('I am happy.', '나는 행복해요.'),
    ('I like apples.', '나는 사과를 좋아해요.'),
    ('This is my book.', '이것은 내 책이에요.'),
    ('I can do it!', '나는 할 수 있어요!'),
    ('Let''s play!', '같이 놀자!'),
    ('It is sunny.', '날씨가 맑아요.'),
    ('I love my family.', '나는 우리 가족을 사랑해요.'),
    ('How are you?', '어떻게 지내니?'),
    ('I am fine.', '나는 잘 지내요.'),
    ('Good night!', '잘 자!'),
    ('See you tomorrow!', '내일 보자!'),
    ('I have a dog.', '나는 강아지를 키워요.'),
    ('The cat is cute.', '고양이가 귀여워요.'),
    ('I like blue.', '나는 파란색을 좋아해요.'),
    ('Please help me.', '도와주세요.'),
    ('You are kind.', '너는 친절해.'),
    ('I am hungry.', '나는 배가 고파요.'),
    ('Let''s eat!', '같이 먹자!'),
    ('It is yummy!', '맛있어요!'),
    ('I can jump.', '나는 뛰어오를 수 있어요.'),
    ('This is my friend.', '이 친구는 내 친구예요.'),
    ('I like to draw.', '나는 그림 그리기를 좋아해요.'),
    ('Wash your hands.', '손을 씻으세요.'),
    ('I am ready!', '나는 준비됐어요!'),
    ('Have a nice day!', '좋은 하루 보내!'),
    ('May I try?', '내가 해 봐도 될까요?'),
    ('You did well!', '잘했어!');
