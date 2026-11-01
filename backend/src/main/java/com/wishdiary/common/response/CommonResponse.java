package com.wishdiary.common.response;

public record CommonResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, "SUCCESS", "요청을 처리했어요.", data);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, "SUCCESS", message, data);
    }

    public static <T> CommonResponse<T> fail(String code, String message) {
        return new CommonResponse<>(false, code, message, null);
    }
}

