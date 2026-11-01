package com.wishdiary.domain.group;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class FamilyManagementAccessDeniedException extends CustomException {

    public FamilyManagementAccessDeniedException() {
        super("FAMILY_MANAGEMENT_ACCESS_DENIED", "가족 관리는 아이 계정에서만 할 수 있어요.", HttpStatus.FORBIDDEN);
    }
}
