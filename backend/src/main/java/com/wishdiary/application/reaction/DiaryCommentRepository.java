package com.wishdiary.application.reaction;

import com.wishdiary.domain.reaction.DiaryComment;
import java.util.List;
import java.util.Map;

public interface DiaryCommentRepository {

    DiaryComment save(DiaryComment diaryComment);

    List<DiaryComment> findAllByDiaryId(Long diaryId);

    Map<Long, Long> countByDiaryIds(List<Long> diaryIds);

    List<DiaryComment> findAllByDiaryIds(List<Long> diaryIds);

    void deleteAllByDiaryId(Long diaryId);
}
