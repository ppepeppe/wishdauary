package com.wishdiary.domain.english;

import java.time.LocalDate;

public final class DailyEnglish {
    private DailyEnglish() {}
    public static int indexFor(LocalDate date, int size) {
        if (size < 1) throw new IllegalArgumentException("The English content pool must not be empty.");
        return Math.floorMod(date.toEpochDay(), size);
    }
}
