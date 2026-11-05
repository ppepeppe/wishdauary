package com.wishdiary.adapter.out.persistence.user;

import com.wishdiary.domain.group.FamilyRelation;
import com.wishdiary.domain.group.GroupMemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "group_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_group_member_group_user", columnNames = {"groupId", "userId"})
)
public class GroupMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMemberRole memberRole;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FamilyRelation familyRelation;

    @Column(length = 100)
    private String relationLabel;

    protected GroupMemberJpaEntity() {
    }

    public GroupMemberJpaEntity(
            Long id,
            Long groupId,
            Long userId,
            GroupMemberRole memberRole,
            FamilyRelation familyRelation,
            String relationLabel
    ) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.memberRole = memberRole;
        this.familyRelation = familyRelation;
        this.relationLabel = relationLabel;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public GroupMemberRole getMemberRole() {
        return memberRole;
    }

    public FamilyRelation getFamilyRelation() {
        return familyRelation;
    }

    public String getRelationLabel() {
        return relationLabel;
    }
}
