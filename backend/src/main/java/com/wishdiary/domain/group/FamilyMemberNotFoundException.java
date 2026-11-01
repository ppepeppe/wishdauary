package com.wishdiary.domain.group;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class FamilyMemberNotFoundException extends CustomException {

    public FamilyMemberNotFoundException(Long groupMemberId) {
        super("FAMILY_MEMBER_NOT_FOUND", "가족 구성원을 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
