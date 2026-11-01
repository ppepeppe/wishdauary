package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ShopProductPriceNotSetException extends CustomException {

    public ShopProductPriceNotSetException() {
        super("SHOP_PRODUCT_PRICE_NOT_SET", "가족이 아직 선물의 클로버 가격을 정하지 않았어요.", HttpStatus.BAD_REQUEST);
    }
}
