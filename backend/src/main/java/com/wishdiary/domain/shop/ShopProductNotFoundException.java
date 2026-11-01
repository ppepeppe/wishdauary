package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ShopProductNotFoundException extends CustomException {

    public ShopProductNotFoundException(Long productId) {
        super("SHOP_PRODUCT_NOT_FOUND", "선물을 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
