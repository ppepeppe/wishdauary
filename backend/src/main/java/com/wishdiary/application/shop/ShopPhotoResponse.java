package com.wishdiary.application.shop;

import com.wishdiary.domain.shop.ShopPhoto;
import java.util.Base64;

final class ShopPhotoResponse {
    private ShopPhotoResponse() { }

    static String imageUrl(ShopPhoto photo, String fallbackUrl) {
        return photo == null ? fallbackUrl : "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(photo.bytes());
    }
}
