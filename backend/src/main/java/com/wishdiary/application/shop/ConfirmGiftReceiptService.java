package com.wishdiary.application.shop;

import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.domain.shop.ChildOnlyGiftRequestException;
import com.wishdiary.domain.shop.GiftRequestNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmGiftReceiptService implements ConfirmGiftReceiptUseCase {
    private final LoadUserPort users;
    private final LoadGiftRequestNotificationPort requests;
    private final SaveGiftRequestNotificationPort savedRequests;
    private final GiftRequestNotificationResponseMapper mapper;

    public ConfirmGiftReceiptService(LoadUserPort users, LoadGiftRequestNotificationPort requests,
            SaveGiftRequestNotificationPort savedRequests, GiftRequestNotificationResponseMapper mapper) {
        this.users = users;
        this.requests = requests;
        this.savedRequests = savedRequests;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public GiftRequestNotificationResponse confirmReceipt(Long childUserId, Long requestId) {
        if (!users.loadById(childUserId).isChild()) throw new ChildOnlyGiftRequestException();
        var gift = requests.loadGiftRequestForUpdate(requestId)
                .orElseThrow(() -> new GiftRequestNotFoundException(requestId));
        return mapper.toResponse(savedRequests.save(gift.receive(childUserId)));
    }
}
