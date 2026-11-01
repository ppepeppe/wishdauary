package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidShopProductUrlException extends CustomException {

    public InvalidShopProductUrlException(String productUrl) {
        super("INVALID_SHOP_PRODUCT_URL", "쇼핑몰 링크는 http:// 또는 https://로 시작하는 주소여야 해요.", HttpStatus.BAD_REQUEST);
    }
}
