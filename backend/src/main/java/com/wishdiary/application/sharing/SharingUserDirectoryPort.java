package com.wishdiary.application.sharing;

import java.util.List;
import java.util.Set;

public interface SharingUserDirectoryPort {
    List<SharingUser> search(String nickname, Long excludedUserId);
    List<SharingUser> searchChildren(String nickname);
    List<SharingUser> findByIds(Set<Long> userIds);
}
