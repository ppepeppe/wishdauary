package com.wishdiary.adapter.out.persistence.user;

import com.wishdiary.domain.user.AppRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppRole appRole;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String avatarPreset = "peach";

    @Column(nullable = false, length = 30)
    private String avatarCharacter = "my-melody";

    @Column(columnDefinition = "text")
    private String avatarImage;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(Long id, String email, String nickname, AppRole appRole, String passwordHash) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.appRole = appRole;
        this.passwordHash = passwordHash;
        this.avatarPreset = "peach";
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public AppRole getAppRole() {
        return appRole;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getAvatarPreset() {
        return avatarPreset;
    }

    public String getAvatarCharacter() { return avatarCharacter; }

    public String getAvatarImage() {
        return avatarImage;
    }

    public void updateProfile(String nickname, String avatarPreset, String avatarImage, String avatarCharacter) {
        this.nickname = nickname;
        this.avatarPreset = avatarPreset;
        this.avatarImage = avatarImage;
        this.avatarCharacter = avatarCharacter;
    }
}
