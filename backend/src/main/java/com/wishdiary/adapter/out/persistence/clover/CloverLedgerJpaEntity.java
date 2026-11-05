package com.wishdiary.adapter.out.persistence.clover;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "clover_ledgers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_clover_ledger_user_reason_ref",
                columnNames = {"userId", "reason", "refId"}
        )
)
public class CloverLedgerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column
    private Long refId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected CloverLedgerJpaEntity() {
    }

    public CloverLedgerJpaEntity(Long id, Long userId, int amount, String reason, Long refId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.refId = refId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public int getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public Long getRefId() {
        return refId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
