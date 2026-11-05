package com.wishdiary.adapter.out.persistence.clover;

import com.wishdiary.application.clover.LoadCloverBalancePort;
import com.wishdiary.application.clover.LoadCloverLedgerHistoryPort;
import com.wishdiary.application.clover.RecordCloverLedgerPort;
import com.wishdiary.domain.clover.CloverLedger;
import com.wishdiary.domain.clover.CloverReason;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CloverLedgerPersistenceAdapter implements RecordCloverLedgerPort, LoadCloverBalancePort, LoadCloverLedgerHistoryPort {

    private final CloverLedgerJpaRepository cloverLedgerJpaRepository;

    public CloverLedgerPersistenceAdapter(CloverLedgerJpaRepository cloverLedgerJpaRepository) {
        this.cloverLedgerJpaRepository = cloverLedgerJpaRepository;
    }

    @Override
    public CloverLedger record(CloverLedger cloverLedger) {
        CloverLedgerJpaEntity cloverLedgerJpaEntity = toEntity(cloverLedger);
        CloverLedgerJpaEntity savedEntity = cloverLedgerJpaRepository.save(cloverLedgerJpaEntity);
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsByUserIdAndReasonAndRefId(Long userId, CloverReason reason, Long refId) {
        return cloverLedgerJpaRepository.existsByUserIdAndReasonAndRefId(userId, reason.name(), refId);
    }

    @Override
    public int loadBalance(Long userId) {
        return cloverLedgerJpaRepository.sumAmountByUserId(userId);
    }

    @Override
    public List<CloverLedger> loadAllByUserId(Long userId) {
        return cloverLedgerJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CloverLedgerJpaEntity toEntity(CloverLedger cloverLedger) {
        return new CloverLedgerJpaEntity(
                cloverLedger.getId(),
                cloverLedger.getUserId(),
                cloverLedger.getAmount(),
                cloverLedger.getReason().name(),
                cloverLedger.getRefId(),
                cloverLedger.getCreatedAt()
        );
    }

    private CloverLedger toDomain(CloverLedgerJpaEntity cloverLedgerJpaEntity) {
        return CloverLedger.reconstitute(
                cloverLedgerJpaEntity.getId(),
                cloverLedgerJpaEntity.getUserId(),
                cloverLedgerJpaEntity.getAmount(),
                CloverReason.valueOf(cloverLedgerJpaEntity.getReason()),
                cloverLedgerJpaEntity.getRefId(),
                cloverLedgerJpaEntity.getCreatedAt()
        );
    }
}
