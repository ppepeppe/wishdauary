package com.wishdiary.application.user;

import java.time.LocalDate;

public interface GetHomeSummaryUseCase {

    HomeSummaryResponse getSummary(Long userId, LocalDate date);
}
