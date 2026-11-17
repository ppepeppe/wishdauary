package com.wishdiary.adapter.out.persistence.shop;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopProductJpaRepository extends JpaRepository<ShopProductJpaEntity, Long> {
    List<ShopProductJpaEntity> findAllByDeletedFalse();
    Optional<ShopProductJpaEntity> findByIdAndDeletedFalse(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ShopProductJpaEntity p where p.id = :id and p.deleted = false")
    Optional<ShopProductJpaEntity> findActiveForUpdate(@Param("id") Long id);

    @Modifying
    @Query("update ShopProductJpaEntity p set p.deleted = true where p.id = :id")
    void archive(@Param("id") Long id);
}
