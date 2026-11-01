package com.wishdiary.domain.group;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class GroupNotFoundException extends CustomException {

    public GroupNotFoundException(Long groupId) {
        super("GROUP_NOT_FOUND", "가족 그룹을 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
