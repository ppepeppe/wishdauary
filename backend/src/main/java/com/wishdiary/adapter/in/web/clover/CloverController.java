package com.wishdiary.adapter.in.web.clover;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.clover.CloverBalanceResponse;
import com.wishdiary.application.clover.CloverHistoryResponse;
import com.wishdiary.application.clover.GetCloverBalanceUseCase;
import com.wishdiary.application.clover.GetCloverHistoryUseCase;
import com.wishdiary.common.response.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clovers")
public class CloverController {

    private final GetCloverBalanceUseCase getCloverBalanceUseCase;
    private final GetCloverHistoryUseCase getCloverHistoryUseCase;

    public CloverController(
            GetCloverBalanceUseCase getCloverBalanceUseCase,
            GetCloverHistoryUseCase getCloverHistoryUseCase
    ) {
        this.getCloverBalanceUseCase = getCloverBalanceUseCase;
        this.getCloverHistoryUseCase = getCloverHistoryUseCase;
    }

    @GetMapping("/balance")
    public CommonResponse<CloverBalanceResponse> getBalance(@Authenticated AuthenticatedSession session) {
        CloverBalanceResponse response = getCloverBalanceUseCase.get(session.userId());
        return CommonResponse.success("Clover balance retrieved.", response);
    }

    @GetMapping("/history")
    public CommonResponse<CloverHistoryResponse> getHistory(@Authenticated AuthenticatedSession session) {
        CloverHistoryResponse response = getCloverHistoryUseCase.getHistory(session.userId());
        return CommonResponse.success("Clover history retrieved.", response);
    }
}
