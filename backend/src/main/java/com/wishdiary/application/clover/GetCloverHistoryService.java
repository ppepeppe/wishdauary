package com.wishdiary.application.clover;

import com.wishdiary.domain.clover.CloverLedger;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetCloverHistoryService implements GetCloverHistoryUseCase {

    private final LoadCloverLedgerHistoryPort loadCloverLedgerHistoryPort;

    public GetCloverHistoryService(LoadCloverLedgerHistoryPort loadCloverLedgerHistoryPort) {
        this.loadCloverLedgerHistoryPort = loadCloverLedgerHistoryPort;
    }

    @Override
    public CloverHistoryResponse getHistory(Long userId) {
        List<CloverHistoryResponse.Entry> entries = loadCloverLedgerHistoryPort.loadAllByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(CloverLedger::getCreatedAt).reversed())
                .map(ledger -> new CloverHistoryResponse.Entry(
                        ledger.getId(),
                        ledger.getAmount(),
                        ledger.getReason().name(),
                        ledger.getRefId(),
                        ledger.getCreatedAt()
                ))
                .toList();
        return new CloverHistoryResponse(entries);
    }
}
