package com.wishdiary.domain.shop;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InsufficientCloverBalanceException extends CustomException {

    public InsufficientCloverBalanceException() {
        super("INSUFFICIENT_CLOVER_BALANCE", "클로버가 부족해요. 일기를 쓰고 다시 부탁해 주세요.", HttpStatus.BAD_REQUEST);
    }
}
