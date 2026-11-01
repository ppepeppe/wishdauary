package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class GiftRequestNotFoundException extends CustomException {

    public GiftRequestNotFoundException(Long requestId) {
        super("GIFT_REQUEST_NOT_FOUND", "선물 부탁을 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
