package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class GiftRequestReceiverNotAllowedException extends CustomException {

    public GiftRequestReceiverNotAllowedException() {
        super(
                "GIFT_REQUEST_RECEIVER_NOT_ALLOWED",
                "내 그룹에 추가한 가족에게만 선물을 부탁할 수 있어요.",
                HttpStatus.FORBIDDEN
        );
    }
}
