package com.wishdiary.adapter.in.web.group;
import com.wishdiary.adapter.in.web.auth.Authenticated;
import com.wishdiary.application.auth.AuthenticatedSession;
import com.wishdiary.application.group.*;
import com.wishdiary.application.sharing.SharingUser;
import com.wishdiary.common.response.CommonResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/family-connections")
public class FamilyConnectionController {
    private final ManageFamilyConnectionsUseCase connections;
    public FamilyConnectionController(ManageFamilyConnectionsUseCase connections) { this.connections = connections; }
    @GetMapping("/children")
    public CommonResponse<List<SharingUser>> search(@Authenticated AuthenticatedSession session, @RequestParam(defaultValue = "") String query) {
        return CommonResponse.success("Children retrieved.", connections.searchChildren(session.userId(), query));
    }
    @GetMapping
    public CommonResponse<List<FamilyConnectionUserResponse>> list(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Connections retrieved.", connections.listConnections(session.userId()));
    }
    @GetMapping("/requests")
    public CommonResponse<List<FamilyConnectionRequestResponse>> requests(@Authenticated AuthenticatedSession session) {
        return CommonResponse.success("Connection requests retrieved.", connections.listRequests(session.userId()));
    }
    @PostMapping("/requests") @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<FamilyConnectionRequestResponse> request(@Authenticated AuthenticatedSession session,
            @Valid @RequestBody SendFamilyConnectionRequest request) {
        return CommonResponse.success("Connection requested.", connections.request(session.userId(), request.childUserId()));
    }
    @PatchMapping("/requests/{requestId}")
    public CommonResponse<FamilyConnectionRequestResponse> process(@Authenticated AuthenticatedSession session,
            @PathVariable Long requestId, @Valid @RequestBody ProcessFamilyConnectionRequest request) {
        return CommonResponse.success("Connection request processed.", connections.process(session.userId(), requestId, request));
    }
}
