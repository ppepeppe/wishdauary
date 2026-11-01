package com.wishdiary.application.reaction;

import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.application.diary.DiaryAudiencePolicy;
import com.wishdiary.domain.diary.Diary;
import com.wishdiary.domain.reaction.DiaryCommentReadNotAllowedException;
import com.wishdiary.domain.reaction.DiaryCommentWriteNotAllowedException;
import org.springframework.stereotype.Component;

@Component
public class ReactionAccessPolicy {

    private final DiaryAudiencePolicy audiencePolicy;

    public ReactionAccessPolicy(DiaryAudiencePolicy audiencePolicy) { this.audiencePolicy = audiencePolicy; }

    public void validateFamilyWriteAccess(LoadedUser familyUser, Diary diary, LoadedUser diaryAuthor) {
        if (!familyUser.isFamily() || !audiencePolicy.canRead(familyUser, diary, diaryAuthor)) {
            throw new DiaryCommentWriteNotAllowedException();
        }
    }

    public void validateCommentReadAccess(LoadedUser viewer, Diary diary, LoadedUser diaryAuthor) {
        if (!audiencePolicy.canRead(viewer, diary, diaryAuthor)) {
            throw new DiaryCommentReadNotAllowedException();
        }
    }
}
