package com.wishdiary.adapter.out.image;

final class InvalidPhotoDataException extends RuntimeException {
    InvalidPhotoDataException() { super("사진을 읽지 못했어요. 다른 사진을 선택해 주세요."); }
    InvalidPhotoDataException(String message) { super(message); }
}
