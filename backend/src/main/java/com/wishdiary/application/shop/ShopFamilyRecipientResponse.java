package com.wishdiary.application.shop;

public record ShopFamilyRecipientResponse(
        Long userId,
        String nickname,
        String relationLabel
) {
}
