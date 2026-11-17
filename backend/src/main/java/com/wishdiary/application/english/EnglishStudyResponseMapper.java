package com.wishdiary.application.english;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.reward.FamilyRewardAccess;
import com.wishdiary.domain.english.EnglishStudy;
import org.springframework.stereotype.Component;

@Component
public class EnglishStudyResponseMapper {
    private final LoadUserPort users;
    private final FamilyRewardAccess access;
    public EnglishStudyResponseMapper(LoadUserPort users, FamilyRewardAccess access) { this.users = users; this.access = access; }
    public EnglishStudyResponse map(EnglishStudy study) {
        var child = users.loadById(study.childUserId());
        var family = users.loadById(study.receiverFamilyUserId());
        return new EnglishStudyResponse(study.id(), child.id(), child.nickname(), family.id(), access.displayName(family),
                study.studyDate(), study.sentence(), study.meaning(), study.status().name().toLowerCase(), study.createdAt(), study.processedAt());
    }
}
