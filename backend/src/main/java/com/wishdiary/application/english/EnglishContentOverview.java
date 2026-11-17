package com.wishdiary.application.english;

import java.time.LocalDate;
import java.util.List;

public record EnglishContentOverview(List<EnglishContentResponse> contents, LocalDate today, Long todayContentId) {}
