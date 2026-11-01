package com.wishdiary.domain.group;

import com.wishdiary.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class GroupNotFoundByInviteCodeException extends CustomException {

    public GroupNotFoundByInviteCodeException(String inviteCode) {
        super("GROUP_NOT_FOUND", "초대 코드를 확인해 주세요. 연결할 가족 그룹을 찾을 수 없어요.", HttpStatus.NOT_FOUND);
    }
}
