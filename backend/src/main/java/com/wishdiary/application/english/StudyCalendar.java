package com.wishdiary.application.english;

import java.time.Instant;
import java.time.LocalDate;

public interface StudyCalendar {
    LocalDate today();
    Instant now();
}
