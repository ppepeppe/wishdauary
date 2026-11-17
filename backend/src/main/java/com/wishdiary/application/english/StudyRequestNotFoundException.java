package com.wishdiary.application.english;

public class StudyRequestNotFoundException extends RuntimeException {
    public StudyRequestNotFoundException() { super("영어 확인 요청을 찾을 수 없어요."); }
}
