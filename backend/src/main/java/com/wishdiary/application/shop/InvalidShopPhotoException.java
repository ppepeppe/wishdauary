package com.wishdiary.application.shop;

public class InvalidShopPhotoException extends RuntimeException {
    public InvalidShopPhotoException() {
        super("사진을 읽지 못했어요. 다른 사진을 선택해 주세요.");
    }

    public InvalidShopPhotoException(String message) {
        super(message);
    }
}
