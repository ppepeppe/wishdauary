package com.wishdiary.application.english;

import java.util.List;

public interface ManageEnglishStudyUseCase {
    EnglishTodayResponse today(Long childId);
    EnglishStudyResponse request(Long childId, Long receiverId, java.time.LocalDate studyDate);
    List<EnglishStudyResponse> pending(Long familyId);
    EnglishStudyResponse review(Long familyId, Long requestId, boolean approved);
}
