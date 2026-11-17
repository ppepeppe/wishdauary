package com.wishdiary.domain.english;

public class InvalidStudyReviewException extends RuntimeException {
    public InvalidStudyReviewException() { super("이미 처리된 영어 확인 요청이에요. 새로고침해 주세요."); }
}
