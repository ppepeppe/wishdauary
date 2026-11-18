package com.wishdiary.application.familyprofile;

import com.wishdiary.application.clover.CloverBalanceResponse;
import com.wishdiary.application.clover.CloverHistoryResponse;
import com.wishdiary.application.shop.GiftRequestNotificationResponse;
import java.util.List;

public record ChildProfileResponse(Long childId, String nickname, CloverBalanceResponse clovers,
        CloverHistoryResponse history, List<GiftRequestNotificationResponse> giftRequests) {}
