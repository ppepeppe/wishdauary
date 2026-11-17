package com.wishdiary.application.english;

import com.wishdiary.domain.english.DailyEnglish;
import java.time.LocalDate;

public record EnglishTodayResponse(LocalDate studyDate, DailyEnglish.Line content, EnglishStudyResponse request) {}
