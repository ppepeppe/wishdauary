package com.wishdiary.domain.wishgoal;

import java.time.LocalDateTime;

public class WishGoal {

    private final Long id;
    private final Long childUserId;
    private final Long productId;
    private final LocalDateTime createdAt;

    private WishGoal(Long id, Long childUserId, Long productId, LocalDateTime createdAt) {
        this.id = id;
        this.childUserId = childUserId;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    public static WishGoal create(Long childUserId, Long productId) {
        return new WishGoal(null, childUserId, productId, LocalDateTime.now());
    }

    public static WishGoal reconstitute(Long id, Long childUserId, Long productId, LocalDateTime createdAt) {
        return new WishGoal(id, childUserId, productId, createdAt);
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
