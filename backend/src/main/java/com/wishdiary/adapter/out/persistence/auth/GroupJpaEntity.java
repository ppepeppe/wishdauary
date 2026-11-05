package com.wishdiary.adapter.out.persistence.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "groups")
public class GroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String inviteCode;

    @Column(nullable = false)
    private boolean inviteCodeRevoked;

    protected GroupJpaEntity() {
    }

    public GroupJpaEntity(Long id, String inviteCode) {
        this(id, inviteCode, false);
    }

    public GroupJpaEntity(Long id, String inviteCode, boolean inviteCodeRevoked) {
        this.id = id;
        this.inviteCode = inviteCode;
        this.inviteCodeRevoked = inviteCodeRevoked;
    }

    public Long getId() {
        return id;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public boolean isInviteCodeRevoked() {
        return inviteCodeRevoked;
    }
}
