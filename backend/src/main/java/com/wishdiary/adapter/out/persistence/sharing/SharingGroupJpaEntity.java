package com.wishdiary.adapter.out.persistence.sharing;

import com.wishdiary.domain.sharing.SharingGroup;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "diary_sharing_groups")
public class SharingGroupJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long ownerId;
    @Column(nullable = false, length = 30)
    private String name;
    private Integer slot;
    @ElementCollection
    @CollectionTable(name = "diary_sharing_group_members", joinColumns = @JoinColumn(name = "sharing_group_id"))
    @Column(name = "user_id", nullable = false)
    private Set<Long> memberIds = new HashSet<>();

    protected SharingGroupJpaEntity() { }

    public SharingGroupJpaEntity(SharingGroup group) {
        id = group.id();
        ownerId = group.ownerId();
        name = group.name();
        slot = group.slot();
        memberIds = new HashSet<>(group.memberIds());
    }

    public SharingGroup toDomain() { return new SharingGroup(id, ownerId, name, slot, memberIds); }
}
