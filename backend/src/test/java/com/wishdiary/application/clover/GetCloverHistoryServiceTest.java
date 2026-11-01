package com.wishdiary.application.clover;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wishdiary.domain.clover.CloverLedger;
import com.wishdiary.domain.clover.CloverReason;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCloverHistoryServiceTest {

    @Mock
    private LoadCloverLedgerHistoryPort loadCloverLedgerHistoryPort;

    private GetCloverHistoryService getCloverHistoryService;

    @BeforeEach
    void setUp() {
        getCloverHistoryService = new GetCloverHistoryService(loadCloverLedgerHistoryPort);
    }

    @Test
    void getHistoryReturnsLedgerEntriesNewestFirst() {
        LocalDateTime older = LocalDateTime.of(2026, 7, 1, 9, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 7, 2, 9, 0);
        Mockito.when(loadCloverLedgerHistoryPort.loadAllByUserId(1L)).thenReturn(List.of(
                CloverLedger.reconstitute(11L, 1L, -25, CloverReason.GIFT_REQUEST, 100L, newer),
                CloverLedger.reconstitute(10L, 1L, 1, CloverReason.DIARY_WRITE, 5L, older)
        ));

        CloverHistoryResponse response = getCloverHistoryService.getHistory(1L);

        assertEquals(2, response.entries().size());
        assertEquals(11L, response.entries().get(0).ledgerId());
        assertEquals(-25, response.entries().get(0).amount());
        assertEquals("GIFT_REQUEST", response.entries().get(0).reason());
        assertEquals(10L, response.entries().get(1).ledgerId());
        assertEquals(1, response.entries().get(1).amount());
        assertEquals("DIARY_WRITE", response.entries().get(1).reason());
    }

    @Test
    void getHistoryReturnsEmptyWhenNoEntries() {
        Mockito.when(loadCloverLedgerHistoryPort.loadAllByUserId(1L)).thenReturn(List.of());

        CloverHistoryResponse response = getCloverHistoryService.getHistory(1L);

        assertEquals(0, response.entries().size());
    }
}
