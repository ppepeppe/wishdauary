package com.wishdiary.application.english;

import com.wishdiary.application.reward.RewardConflictException;
import com.wishdiary.domain.english.EnglishContent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnglishContentService implements ManageEnglishContentsUseCase {
    private final EnglishContentPort contents;
    private final GetDailyEnglishContentUseCase daily;
    private final StudyCalendar calendar;
    public EnglishContentService(EnglishContentPort contents, GetDailyEnglishContentUseCase daily, StudyCalendar calendar) {
        this.contents = contents; this.daily = daily; this.calendar = calendar;
    }
    public EnglishContentOverview list() {
        var date = calendar.today();
        var pool = contents.list();
        Long todayId = pool.isEmpty() ? null : daily.get(date).id();
        return new EnglishContentOverview(pool.stream().map(EnglishContentResponse::from).toList(), date, todayId);
    }
    public EnglishContentResponse create(CreateEnglishContentRequest request) {
        return EnglishContentResponse.from(contents.save(EnglishContent.create(request.sentence(), request.meaning(), calendar.now())));
    }
    public EnglishContentResponse update(Long id, UpdateEnglishContentRequest request) {
        var content = contents.findForUpdate(id).orElseThrow(EnglishContentNotFoundException::new);
        if (content.version() != request.version()) {
            throw new RewardConflictException("다른 곳에서 수정한 문장이에요. 목록을 새로고침한 뒤 다시 수정해 주세요.");
        }
        return EnglishContentResponse.from(contents.save(content.revise(request.sentence(), request.meaning(), calendar.now())));
    }
}
