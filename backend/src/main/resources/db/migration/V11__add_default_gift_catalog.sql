-- Shared gifts are real requestable rewards. Custom gifts and request snapshots stay intact.
insert into shop_products (title, description, image_url, clover_price, source, created_at)
values
    ('좋아하는 간식', '가족과 함께 좋아하는 간식을 하나 골라요.', '/assets/shop/snack.svg', 5, 'PRESET', current_timestamp),
    ('문구 고르기', '마음에 드는 연필이나 작은 문구를 골라요.', '/assets/shop/pencils.svg', 10, 'PRESET', current_timestamp),
    ('읽고 싶은 책', '읽어보고 싶었던 책 한 권을 가족과 함께 골라요.', '/assets/shop/book.svg', 15, 'PRESET', current_timestamp),
    ('가족 영화 시간', '가족과 함께 보고 싶은 영화를 골라 즐겨요.', '/assets/shop/movie.svg', 20, 'PRESET', current_timestamp);
