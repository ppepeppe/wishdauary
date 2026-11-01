package com.wishdiary.adapter.in.web.group;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.common.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/family")
public class FamilyController {
    @RequestMapping(value = {"/invite-code", "/invite-code/regenerate", "/invite-code/revoke", "/members", "/members/{id}"},
            method = {RequestMethod.GET, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<CommonResponse<Void>> movedToGroups(@Authenticated AuthenticatedSession session) {
        return ResponseEntity.status(HttpStatus.GONE).body(CommonResponse.fail(
                "FAMILY_MANAGEMENT_MOVED", "가족 연결은 마이페이지의 그룹 관리에서 변경해 주세요."));
    }
}
