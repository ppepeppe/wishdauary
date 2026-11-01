package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ChildOnlyGiftRequestException extends CustomException {

    public ChildOnlyGiftRequestException() {
        super("CHILD_ONLY_GIFT_REQUEST", "선물 부탁은 아이 계정에서만 할 수 있어요.", HttpStatus.FORBIDDEN);
    }
}
