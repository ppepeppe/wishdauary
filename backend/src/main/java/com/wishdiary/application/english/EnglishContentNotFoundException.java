package com.wishdiary.application.english;

public class EnglishContentNotFoundException extends RuntimeException {
    public EnglishContentNotFoundException() { super("영어 문장을 찾을 수 없어요."); }
}
