package com.wishdiary.application.group;
import com.wishdiary.application.sharing.SharingUser;
import java.util.List;
public interface ManageFamilyConnectionsUseCase {
    List<SharingUser> searchChildren(Long familyId, String query);
    List<FamilyConnectionUserResponse> listConnections(Long userId);
    List<FamilyConnectionRequestResponse> listRequests(Long userId);
    FamilyConnectionRequestResponse request(Long familyId, Long childId);
    FamilyConnectionRequestResponse process(Long childId, Long requestId, ProcessFamilyConnectionRequest request);
}
