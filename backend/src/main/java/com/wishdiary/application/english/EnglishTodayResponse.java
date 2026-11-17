package com.wishdiary.application.english;

import com.wishdiary.domain.english.EnglishLesson;
import java.time.LocalDate;

public record EnglishTodayResponse(LocalDate studyDate, EnglishLesson content, EnglishStudyResponse request) {}
