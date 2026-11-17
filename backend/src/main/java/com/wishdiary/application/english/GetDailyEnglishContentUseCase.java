package com.wishdiary.application.english;

import com.wishdiary.domain.english.EnglishContent;
import java.time.LocalDate;

public interface GetDailyEnglishContentUseCase {
    EnglishContent get(LocalDate date);
}
