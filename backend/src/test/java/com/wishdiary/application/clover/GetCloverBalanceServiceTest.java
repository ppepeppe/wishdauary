package com.wishdiary.application.clover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCloverBalanceServiceTest {

    @Mock
    private LoadCloverBalancePort loadCloverBalancePort;

    private GetCloverBalanceService getCloverBalanceService;

    @BeforeEach
    void setUp() {
        getCloverBalanceService = new GetCloverBalanceService(loadCloverBalancePort);
    }

    @Test
    void getReturnsCurrentBalance() {
        when(loadCloverBalancePort.loadBalance(1L)).thenReturn(3);

        CloverBalanceResponse response = getCloverBalanceService.get(1L);

        assertEquals(1L, response.userId());
        assertEquals(3, response.balance());
    }
}
