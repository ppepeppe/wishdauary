package com.wishdiary.adapter.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
public class UserStatsJpaEntity {

    @Id
    private Long userId;

    @Column(nullable = false)
    private int currentStreak;

    @Column(nullable = false)
    private int maxStreak;

    private LocalDate lastWrittenDate;

    protected UserStatsJpaEntity() {
    }

    public UserStatsJpaEntity(Long userId, int currentStreak, int maxStreak, LocalDate lastWrittenDate) {
        this.userId = userId;
        this.currentStreak = currentStreak;
        this.maxStreak = maxStreak;
        this.lastWrittenDate = lastWrittenDate;
    }

    public Long getUserId() {
        return userId;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public LocalDate getLastWrittenDate() {
        return lastWrittenDate;
    }
}
