package com.wishdiary.domain.group;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InviteCodeGenerationFailedException extends CustomException {

    public InviteCodeGenerationFailedException() {
        super("INVITE_CODE_GENERATION_FAILED", "초대 코드를 만들지 못했어요. 잠시 후 다시 시도해 주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
