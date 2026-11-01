package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ShopProductPriceSettingNotAllowedException extends CustomException {

    public ShopProductPriceSettingNotAllowedException(Long productId) {
        super("SHOP_PRODUCT_PRICE_SETTING_NOT_ALLOWED", "이 선물의 가격을 정할 권한이 없어요.", HttpStatus.FORBIDDEN);
    }
}
