package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class GiftRequestAlreadyProcessedException extends CustomException {

    public GiftRequestAlreadyProcessedException(Long requestId) {
        super("GIFT_REQUEST_ALREADY_PROCESSED", "이미 처리한 선물 부탁이에요.", HttpStatus.CONFLICT);
    }
}
