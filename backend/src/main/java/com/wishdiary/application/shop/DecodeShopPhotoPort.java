package com.wishdiary.application.shop;

import com.wishdiary.domain.shop.ShopPhoto;

public interface DecodeShopPhotoPort {
    ShopPhoto decode(String imageData);
}
