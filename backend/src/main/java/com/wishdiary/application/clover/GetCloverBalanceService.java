package com.wishdiary.application.clover;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetCloverBalanceService implements GetCloverBalanceUseCase {

    private final LoadCloverBalancePort loadCloverBalancePort;

    public GetCloverBalanceService(LoadCloverBalancePort loadCloverBalancePort) {
        this.loadCloverBalancePort = loadCloverBalancePort;
    }

    @Override
    public CloverBalanceResponse get(Long userId) {
        int balance = loadCloverBalancePort.loadBalance(userId);
        return new CloverBalanceResponse(userId, balance);
    }
}
