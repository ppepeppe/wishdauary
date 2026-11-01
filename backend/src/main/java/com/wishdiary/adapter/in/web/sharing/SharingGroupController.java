package com.wishdiary.adapter.in.web.sharing;

import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.sharing.*;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sharing-groups")
public class SharingGroupController {
    private final ManageSharingGroupsUseCase groups;

    public SharingGroupController(ManageSharingGroupsUseCase groups) { this.groups = groups; }

    @GetMapping
    public CommonResponse<List<SharingGroupResponse>> list(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Sharing groups retrieved.", groups.list(session.userId()));
    }

    @GetMapping("/users")
    public CommonResponse<List<SharingUser>> search(@Authenticated AuthenticatedSession session,
                                                   @RequestParam(defaultValue = "") String query) {
        return CommonResponse.success("Users retrieved.", groups.searchUsers(session.userId(), query));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<SharingGroupResponse> create(@Authenticated AuthenticatedSession session,
                                                        @Valid @RequestBody SharingGroupRequest request) {
        return CommonResponse.success("Sharing group created.", groups.create(session.userId(), request));
    }

    @PatchMapping("/{groupId}")
    public CommonResponse<SharingGroupResponse> update(@Authenticated AuthenticatedSession session,
            @PathVariable Long groupId, @Valid @RequestBody SharingGroupRequest request) {
        return CommonResponse.success("Sharing group updated.", groups.update(session.userId(), groupId, request));
    }

    @DeleteMapping("/{groupId}")
    public CommonResponse<Void> delete(@Authenticated AuthenticatedSession session, @PathVariable Long groupId) {
        groups.delete(session.userId(), groupId);
        return CommonResponse.success("Sharing group deleted.", null);
    }
}
