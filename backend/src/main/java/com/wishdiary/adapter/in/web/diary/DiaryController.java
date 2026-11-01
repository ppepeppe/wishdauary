package com.wishdiary.adapter.in.web.diary;

import com.wishdiary.application.diary.GetSharedDiaryFeedUseCase;
import com.wishdiary.application.diary.SharedDiaryResponse;
import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.diary.CreateDiaryRequest;
import com.wishdiary.application.diary.CreateDiaryResponse;
import com.wishdiary.application.diary.CreateDiaryUseCase;
import com.wishdiary.application.diary.DeleteDiaryUseCase;
import com.wishdiary.application.diary.DiaryDetailResponse;
import com.wishdiary.application.diary.FamilyDiaryFeedResponse;
import com.wishdiary.application.diary.GetFamilyDiaryFeedUseCase;
import com.wishdiary.application.diary.GetDiaryDetailUseCase;
import com.wishdiary.application.diary.GetMyDiaryListUseCase;
import com.wishdiary.application.diary.GetTodayDiaryExistenceUseCase;
import com.wishdiary.application.diary.MyDiarySummaryResponse;
import com.wishdiary.application.diary.TodayDiaryExistenceResponse;
import com.wishdiary.application.diary.UpdateDiaryRequest;
import com.wishdiary.application.diary.UpdateDiaryUseCase;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diaries")
public class DiaryController {

    private final GetSharedDiaryFeedUseCase sharedFeed;
    private final CreateDiaryUseCase createDiaryUseCase;
    private final GetTodayDiaryExistenceUseCase getTodayDiaryExistenceUseCase;
    private final GetMyDiaryListUseCase getMyDiaryListUseCase;
    private final GetFamilyDiaryFeedUseCase getFamilyDiaryFeedUseCase;
    private final GetDiaryDetailUseCase getDiaryDetailUseCase;
    private final UpdateDiaryUseCase updateDiaryUseCase;
    private final DeleteDiaryUseCase deleteDiaryUseCase;

    public DiaryController(
            CreateDiaryUseCase createDiaryUseCase,
            GetTodayDiaryExistenceUseCase getTodayDiaryExistenceUseCase,
            GetMyDiaryListUseCase getMyDiaryListUseCase,
            GetFamilyDiaryFeedUseCase getFamilyDiaryFeedUseCase,
            GetDiaryDetailUseCase getDiaryDetailUseCase,
            UpdateDiaryUseCase updateDiaryUseCase,
            DeleteDiaryUseCase deleteDiaryUseCase,
            GetSharedDiaryFeedUseCase sharedFeed
    ) {
        this.sharedFeed = sharedFeed;
        this.createDiaryUseCase = createDiaryUseCase;
        this.getTodayDiaryExistenceUseCase = getTodayDiaryExistenceUseCase;
        this.getMyDiaryListUseCase = getMyDiaryListUseCase;
        this.getFamilyDiaryFeedUseCase = getFamilyDiaryFeedUseCase;
        this.getDiaryDetailUseCase = getDiaryDetailUseCase;
        this.updateDiaryUseCase = updateDiaryUseCase;
        this.deleteDiaryUseCase = deleteDiaryUseCase;
    }

    @GetMapping("/shared-feed")
    public CommonResponse<List<SharedDiaryResponse>> sharedFeed(
            @Authenticated AuthenticatedSession session, @RequestParam(defaultValue = "0") int page) {
        return CommonResponse.success("Shared diaries retrieved.", sharedFeed.getSharedFeed(session.userId(), page));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<CreateDiaryResponse> createDiary(
            @Authenticated AuthenticatedSession session,
            @Valid @RequestBody CreateDiaryRequest request
    ) {
        CreateDiaryResponse response = createDiaryUseCase.create(session.userId(), request);
        return CommonResponse.success("Diary created.", response);
    }

    @GetMapping("/existence")
    public CommonResponse<TodayDiaryExistenceResponse> getDiaryExistence(
            @Authenticated AuthenticatedSession session,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        TodayDiaryExistenceResponse response = getTodayDiaryExistenceUseCase.get(session.userId(), date);
        return CommonResponse.success("Diary existence retrieved.", response);
    }

    @GetMapping
    public CommonResponse<List<MyDiarySummaryResponse>> getMyDiaries(@Authenticated AuthenticatedSession session) {
        List<MyDiarySummaryResponse> response = getMyDiaryListUseCase.get(session.userId());
        return CommonResponse.success("Diary list retrieved.", response);
    }

    @GetMapping("/{diaryId}")
    public CommonResponse<DiaryDetailResponse> getDiaryDetail(
            @Authenticated AuthenticatedSession session,
            @PathVariable Long diaryId
    ) {
        DiaryDetailResponse response = getDiaryDetailUseCase.getDetail(session.userId(), diaryId);
        return CommonResponse.success("Diary detail retrieved.", response);
    }

    @PatchMapping("/{diaryId}")
    public CommonResponse<DiaryDetailResponse> updateDiary(
            @Authenticated AuthenticatedSession session,
            @PathVariable Long diaryId,
            @Valid @RequestBody UpdateDiaryRequest request
    ) {
        DiaryDetailResponse response = updateDiaryUseCase.update(session.userId(), diaryId, request);
        return CommonResponse.success("Diary updated.", response);
    }

    @DeleteMapping("/{diaryId}")
    public CommonResponse<Void> deleteDiary(
            @Authenticated AuthenticatedSession session,
            @PathVariable Long diaryId
    ) {
        deleteDiaryUseCase.delete(session.userId(), diaryId);
        return CommonResponse.success("Diary deleted.", null);
    }

    @GetMapping("/family-feed")
    public CommonResponse<List<FamilyDiaryFeedResponse>> getFamilyFeed(@Authenticated AuthenticatedSession session) {
        List<FamilyDiaryFeedResponse> response = getFamilyDiaryFeedUseCase.getFamilyFeed(session.userId());
        return CommonResponse.success("Family diary feed retrieved.", response);
    }
}
