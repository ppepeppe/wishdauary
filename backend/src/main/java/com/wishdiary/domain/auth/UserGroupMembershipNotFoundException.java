package com.wishdiary.domain.auth;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserGroupMembershipNotFoundException extends CustomException {

    public UserGroupMembershipNotFoundException(Long userId) {
        super("GROUP_MEMBERSHIP_NOT_FOUND", "연결된 가족 그룹을 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
