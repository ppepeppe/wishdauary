package com.wishdiary.adapter.out.persistence.sharing;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SharingGroupJpaRepository extends JpaRepository<SharingGroupJpaEntity, Long> {
    @EntityGraph(attributePaths = "memberIds")
    List<SharingGroupJpaEntity> findAllByOwnerIdAndSlotIsNotNullOrderBySlot(Long ownerId);

    @Query("select count(g) from SharingGroupJpaEntity g join g.memberIds m "
            + "where g.ownerId = :ownerId and g.id in :groupIds and g.slot is not null and m = :viewerId")
    long countMembership(@Param("ownerId") Long ownerId, @Param("groupIds") List<Long> groupIds,
                         @Param("viewerId") Long viewerId);

    @Query(value = """
            select distinct m.user_id from diary_sharing_groups g
            join diary_sharing_group_members m on m.sharing_group_id = g.id
            join users u on u.id = m.user_id
            where g.owner_id = :childId and g.slot is not null and u.app_role = 'FAMILY'
            order by m.user_id
            """, nativeQuery = true)
    List<Long> findFamilyUserIds(@Param("childId") Long childId);

    @Query(value = """
            select distinct g.owner_id from diary_sharing_groups g
            join diary_sharing_group_members m on m.sharing_group_id = g.id
            join users u on u.id = m.user_id
            where m.user_id = :familyId and g.slot is not null and u.app_role = 'FAMILY'
            order by g.owner_id
            """, nativeQuery = true)
    List<Long> findChildUserIds(@Param("familyId") Long familyId);
}
