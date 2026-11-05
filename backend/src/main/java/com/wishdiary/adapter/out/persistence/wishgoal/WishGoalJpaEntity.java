package com.wishdiary.adapter.out.persistence.wishgoal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "wish_goals")
public class WishGoalJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long childUserId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected WishGoalJpaEntity() {
    }

    public WishGoalJpaEntity(Long id, Long childUserId, Long productId, LocalDateTime createdAt) {
        this.id = id;
        this.childUserId = childUserId;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getChildUserId() {
        return childUserId;
    }

    public Long getProductId() {
        return productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
