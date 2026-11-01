package com.wishdiary.application.clover;

public interface GetCloverHistoryUseCase {

    CloverHistoryResponse getHistory(Long userId);
}
