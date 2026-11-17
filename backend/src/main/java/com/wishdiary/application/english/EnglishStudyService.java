package com.wishdiary.application.english;

import com.wishdiary.application.clover.RecordCloverLedgerPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.reward.*;
import com.wishdiary.application.user.UpdateUserStreakPort;
import com.wishdiary.domain.clover.*;
import com.wishdiary.domain.english.*;
import com.wishdiary.domain.group.FamilyConnectionAccessDeniedException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnglishStudyService implements ManageEnglishStudyUseCase {
    private final EnglishStudyPort studies;
    private final StudyCalendar calendar;
    private final FamilyRewardAccess access;
    private final FamilyConnectionPort connections;
    private final UpdateUserStreakPort locks;
    private final RecordCloverLedgerPort ledger;
    private final EnglishStudyResponseMapper mapper;
    private final GetDailyEnglishContentUseCase daily;
    public EnglishStudyService(EnglishStudyPort studies, StudyCalendar calendar, FamilyRewardAccess access,
            FamilyConnectionPort connections, UpdateUserStreakPort locks, RecordCloverLedgerPort ledger,
            EnglishStudyResponseMapper mapper, GetDailyEnglishContentUseCase daily) {
        this.studies = studies; this.calendar = calendar; this.access = access; this.connections = connections;
        this.locks = locks; this.ledger = ledger; this.mapper = mapper; this.daily = daily;
    }
    @Transactional
    public EnglishTodayResponse today(Long childId) {
        access.requireChild(childId);
        var date = calendar.today();
        var existing = studies.findByDay(childId, date);
        var lesson = existing.map(EnglishLesson::snapshot).orElseGet(() -> daily.get(date).lesson());
        return new EnglishTodayResponse(date, lesson, existing.map(mapper::map).orElse(null));
    }
    @Transactional
    public EnglishStudyResponse request(Long childId, Long receiverId, java.time.LocalDate studyDate, Long contentId, Long contentVersion) {
        access.requireChild(childId);
        access.requireFamily(receiverId);
        locks.lockUser(childId);
        access.requireConnection(childId, receiverId);
        var date = calendar.today();
        if (!date.equals(studyDate)) throw new RewardConflictException("날짜가 바뀌었어요. 오늘의 영어를 새로 불러와 주세요.");
        var existing = studies.findByDay(childId, date);
        if (existing.isEmpty()) {
            var content = daily.get(date);
            if (!java.util.Objects.equals(content.id(), contentId) || !java.util.Objects.equals(content.version(), contentVersion)) {
                throw new RewardConflictException("영어 문장이 수정됐어요. 최신 내용을 다시 읽은 뒤 확인을 요청해 주세요.");
            }
            return mapper.map(studies.save(EnglishStudy.request(childId, receiverId, date, content.lesson(), calendar.now())));
        }
        var previous = existing.get();
        if (previous.status() == EnglishStudy.Status.APPROVED) return mapper.map(previous);
        if (previous.status() == EnglishStudy.Status.REJECTED) return mapper.map(studies.save(previous.resubmit(receiverId, calendar.now())));
        if (!previous.receiverFamilyUserId().equals(receiverId)) throw new RewardConflictException("이미 다른 가족에게 확인을 요청했어요.");
        return mapper.map(previous);
    }
    public List<EnglishStudyResponse> pending(Long familyId) {
        access.requireFamily(familyId);
        var children = connections.findChildUserIds(familyId);
        return studies.findPending(familyId).stream().filter(study -> children.contains(study.childUserId())).map(mapper::map).toList();
    }
    @Transactional
    public EnglishStudyResponse review(Long familyId, Long requestId, boolean approved) {
        var family = access.requireFamily(familyId);
        Long childId = studies.findChildId(requestId).orElseThrow(StudyRequestNotFoundException::new);
        locks.lockUser(childId);
        var study = studies.findById(requestId).orElseThrow(StudyRequestNotFoundException::new);
        if (!study.receiverFamilyUserId().equals(familyId)) throw new FamilyConnectionAccessDeniedException();
        access.requireConnection(childId, familyId);
        var reviewed = study.review(approved, calendar.now());
        if (approved) {
            long day = study.studyDate().toEpochDay();
            if (!ledger.existsByUserIdAndReasonAndRefId(childId, CloverReason.ENGLISH_STUDY, day)) {
                ledger.record(CloverLedger.create(childId, 1, CloverReason.ENGLISH_STUDY, day)
                        .withDetails(new CloverDetails(study.sentence(), familyId, access.displayName(family), null)));
            }
        }
        return mapper.map(studies.save(reviewed));
    }
}
