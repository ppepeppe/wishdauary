package com.wishdiary.application.english;

public class EnglishContentUnavailableException extends RuntimeException {
    public EnglishContentUnavailableException() { super("등록된 영어 문장이 없어요. 잠시 후 다시 확인해 주세요."); }
}
