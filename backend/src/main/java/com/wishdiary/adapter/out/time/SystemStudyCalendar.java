package com.wishdiary.adapter.out.time;

import com.wishdiary.application.english.StudyCalendar;
import java.time.*;
import org.springframework.stereotype.Component;

@Component
public class SystemStudyCalendar implements StudyCalendar {
    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");
    public LocalDate today() { return LocalDate.now(KOREA); }
    public Instant now() { return Instant.now(); }
}
