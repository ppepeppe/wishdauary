package com.wishdiary.application.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.application.clover.LoadCloverBalancePort;
import com.wishdiary.application.clover.RecordCloverLedgerPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.clover.CloverLedger;
import com.wishdiary.domain.clover.CloverReason;
import com.wishdiary.domain.group.FamilyRelation;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.notification.GiftRequestNotification;
import com.wishdiary.domain.notification.GiftRequestStatus;
import com.wishdiary.domain.shop.InsufficientCloverBalanceException;
import com.wishdiary.domain.shop.InvalidShopProductUrlException;
import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductSource;
import com.wishdiary.domain.shop.ShopProductNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private LoadShopProductPort loadShopProductPort;

    @Mock
    private SaveShopProductPort saveShopProductPort;

    @Mock
    private SaveGiftRequestNotificationPort saveGiftRequestNotificationPort;

    @Mock
    private LoadGiftRequestNotificationPort loadGiftRequestNotificationPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private FamilyConnectionPort familyConnections;

    @Mock
    private LoadCloverBalancePort loadCloverBalancePort;

    @Mock
    private RecordCloverLedgerPort recordCloverLedgerPort;

    private ShopService shopService;

    @BeforeEach
    void setUp() {
        shopService = new ShopService(
                loadShopProductPort,
                saveShopProductPort,
                saveGiftRequestNotificationPort,
                loadGiftRequestNotificationPort,
                loadUserPort,
                familyConnections,
                loadCloverBalancePort,
                recordCloverLedgerPort,
                new ShopProductResponseMapper(),
                new GiftRequestResponseMapper(),
                new GiftRequestNotificationResponseMapper(),
                data -> null
        );
    }

    @Test
    void getProductsShowsCatalogAndThisChildsGiftsWithoutCreatingProductsOnRead() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadShopProductPort.loadAll()).thenReturn(List.of(
                ShopProduct.reconstitute(10L, null, "기본 선물", "예시", null, null, 8, null, ShopProductSource.PRESET, java.time.LocalDateTime.now()),
                ShopProduct.reconstitute(11L, 1L, "내 선물", "직접 적음", null, null, 0, null, ShopProductSource.CUSTOM, java.time.LocalDateTime.now()),
                ShopProduct.reconstitute(12L, 3L, "다른 아이 선물", "다른 가족", null, null, 10, null, ShopProductSource.CUSTOM, java.time.LocalDateTime.now()),
                ShopProduct.reconstitute(13L, 1L, "클로버가 정해진 내 선물", "확인 완료", null, null, 6, null, ShopProductSource.CUSTOM, java.time.LocalDateTime.now())
        ));

        List<ShopProductResponse> products = shopService.getProducts(1L);

        assertEquals(List.of(13L, 10L), products.stream().map(ShopProductResponse::productId).toList());
        verify(saveShopProductPort, never()).save(any(ShopProduct.class));
    }

    @ParameterizedTest
    @EnumSource(ShopProductSource.class)
    void requestGiftDeductsCloverAndRecordsLedger(ShopProductSource source) {
        LoadedUser child = childUser();
        LoadedUser mother = familyUser(2L, FamilyRelation.MOTHER, "엄마");
        ShopProduct product = ShopProduct.reconstitute(10L, source == ShopProductSource.PRESET ? null : 1L, "문구 세트", "연필과 지우개", null, null, 25, null, source, java.time.LocalDateTime.now());
        GiftRequestNotification savedNotification = GiftRequestNotification.reconstitute(
                100L,
                10L,
                "문구 세트",
                null,
                null,
                25,
                null,
                1L,
                "미아",
                2L,
                "엄마",
                "선물 받고 싶어요",
                GiftRequestStatus.PENDING,
                null,
                null,
                java.time.LocalDateTime.now()
        );

        when(loadUserPort.loadById(1L)).thenReturn(child);
        when(loadUserPort.loadById(2L)).thenReturn(mother);
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.of(product));
        when(loadCloverBalancePort.loadBalance(1L)).thenReturn(30);
        when(saveGiftRequestNotificationPort.save(any(GiftRequestNotification.class))).thenReturn(savedNotification);

        GiftRequestResponse response = shopService.request(1L, new RequestGiftRequest(10L, 2L, "선물 받고 싶어요"));

        assertEquals(100L, response.notificationId());
        assertEquals(5, response.remainingCloverBalance());

        ArgumentCaptor<CloverLedger> ledgerCaptor = ArgumentCaptor.forClass(CloverLedger.class);
        verify(recordCloverLedgerPort).record(ledgerCaptor.capture());
        assertEquals(1L, ledgerCaptor.getValue().getUserId());
        assertEquals(-25, ledgerCaptor.getValue().getAmount());
        assertEquals(CloverReason.GIFT_REQUEST, ledgerCaptor.getValue().getReason());
        assertEquals(100L, ledgerCaptor.getValue().getRefId());
    }

    @Test
    void requestGiftCannotUseAnotherChildsCustomProduct() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.of(
                ShopProduct.reconstitute(10L, 3L, "다른 아이 선물", "비공개 선물", null, null, 5, null, ShopProductSource.CUSTOM, java.time.LocalDateTime.now())
        ));

        assertThrows(ShopProductNotFoundException.class,
                () -> shopService.request(1L, new RequestGiftRequest(10L, 2L, "갖고 싶어요")));

        verify(saveGiftRequestNotificationPort, never()).save(any(GiftRequestNotification.class));
        verify(recordCloverLedgerPort, never()).record(any(CloverLedger.class));
    }

    @Test
    void requestGiftFailsWhenCloverBalanceIsBelowPrice() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadUserPort.loadById(2L)).thenReturn(familyUser(2L, FamilyRelation.FATHER, "아빠"));
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.of(
                ShopProduct.reconstitute(10L, 1L, "키링", "작은 키링", null, null, 18, null, ShopProductSource.CUSTOM, java.time.LocalDateTime.now())
        ));
        when(loadCloverBalancePort.loadBalance(1L)).thenReturn(7);

        assertThrows(
                InsufficientCloverBalanceException.class,
                () -> shopService.request(1L, new RequestGiftRequest(10L, 2L, "선물 받고 싶어요"))
        );

        verify(saveGiftRequestNotificationPort, never()).save(any(GiftRequestNotification.class));
        verify(recordCloverLedgerPort, never()).record(any(CloverLedger.class));
    }

    @Test
    void getRecipientsReturnsConnectedFamilyMembers() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(familyConnections.findFamilyUserIds(1L)).thenReturn(List.of(2L, 3L, 4L));
        when(loadUserPort.loadByIds(List.of(2L, 3L, 4L))).thenReturn(java.util.Map.of(
                2L, familyUser(2L, FamilyRelation.MOTHER, "엄마"),
                3L, familyUser(3L, FamilyRelation.UNCLE, "삼촌"),
                4L, familyUser(4L, FamilyRelation.GRANDFATHER, "할아버지")));


        List<ShopFamilyRecipientResponse> recipients = shopService.getRecipients(1L);

        assertEquals(3, recipients.size());
        assertEquals("엄마", recipients.get(0).relationLabel());
        assertEquals("삼촌", recipients.get(1).relationLabel());
        assertEquals("할아버지", recipients.get(2).relationLabel());
    }

    @Test
    void createCustomProductFailsWhenProductUrlIsInvalid() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());

        assertThrows(
                InvalidShopProductUrlException.class,
                () -> shopService.create(1L, new CreateShopProductRequest(
                        "카드",
                        "포켓몬 카드",
                        null,
                        "www.coupang.com/product",
                        null
                ))
        );
    }

    @Test
    void processGiftRequestApprovesPendingRequest() {
        GiftRequestNotification notification = pendingGiftRequest();

        when(loadUserPort.loadById(2L)).thenReturn(familyUser(2L, FamilyRelation.MOTHER, "엄마"));
        when(loadGiftRequestNotificationPort.loadGiftRequestById(100L)).thenReturn(Optional.of(notification));
        when(saveGiftRequestNotificationPort.save(any(GiftRequestNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiftRequestNotificationResponse response = shopService.process(
                2L,
                100L,
                new ProcessGiftRequestRequest(ProcessGiftRequestAction.APPROVE)
        );

        assertEquals("approved", response.status());
        assertEquals(2L, response.processedByFamilyUserId());
        verify(recordCloverLedgerPort, never()).record(any(CloverLedger.class));
    }

    @Test
    void processGiftRequestRejectsPendingRequest() {
        GiftRequestNotification notification = pendingGiftRequest();

        when(loadUserPort.loadById(2L)).thenReturn(familyUser(2L, FamilyRelation.MOTHER, "엄마"));
        when(loadGiftRequestNotificationPort.loadGiftRequestById(100L)).thenReturn(Optional.of(notification));
        when(saveGiftRequestNotificationPort.save(any(GiftRequestNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiftRequestNotificationResponse response = shopService.process(
                2L,
                100L,
                new ProcessGiftRequestRequest(ProcessGiftRequestAction.REJECT)
        );

        assertEquals("rejected", response.status());
        assertEquals(2L, response.processedByFamilyUserId());

        ArgumentCaptor<CloverLedger> ledgerCaptor = ArgumentCaptor.forClass(CloverLedger.class);
        verify(recordCloverLedgerPort).record(ledgerCaptor.capture());
        assertEquals(1L, ledgerCaptor.getValue().getUserId());
        assertEquals(25, ledgerCaptor.getValue().getAmount());
        assertEquals(CloverReason.GIFT_REQUEST_REFUND, ledgerCaptor.getValue().getReason());
        assertEquals(100L, ledgerCaptor.getValue().getRefId());
    }

    @Test
    void getPricingRequestsReturnsUnpricedCustomProductsInFamilyGroup() {
        when(loadUserPort.loadById(2L)).thenReturn(familyUser(2L, FamilyRelation.MOTHER, "엄마"));
        when(familyConnections.findChildUserIds(2L)).thenReturn(List.of(1L));
        when(loadShopProductPort.loadAll()).thenReturn(List.of(
                ShopProduct.reconstitute(20L, 1L, "카드", "포켓몬 카드", null, "https://shop.example/card", 0, null, ShopProductSource.CUSTOM, java.time.LocalDateTime.now()),
                ShopProduct.reconstitute(21L, 1L, "키링", "캐릭터 키링", null, "https://shop.example/keyring", 10, 12000, ShopProductSource.CUSTOM, java.time.LocalDateTime.now()),
                ShopProduct.reconstitute(22L, null, "문구", "문구 세트", null, null, 25, null, ShopProductSource.PRESET, java.time.LocalDateTime.now())
        ));

        List<ShopProductResponse> responses = shopService.getPricingRequests(2L);

        assertEquals(1, responses.size());
        assertEquals(20L, responses.get(0).productId());
        assertEquals("https://shop.example/card", responses.get(0).productUrl());
    }

    @Test
    void getSentRequestsReturnsChildGiftRequests() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadGiftRequestNotificationPort.loadAllByRequesterChildUserId(1L)).thenReturn(List.of(pendingGiftRequest()));

        List<GiftRequestNotificationResponse> responses = shopService.getSentRequests(1L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).notificationId());
        assertEquals("pending", responses.get(0).status());
        assertEquals("엄마", responses.get(0).receiverRelationLabel());
    }

    @Test
    void setPriceUpdatesCustomProductInFamilyGroup() {
        ShopProduct product = ShopProduct.reconstitute(20L, 1L, "카드", "포켓몬 카드", null, "https://shop.example/card", 0, null, ShopProductSource.CUSTOM, java.time.LocalDateTime.now());
        when(loadUserPort.loadById(2L)).thenReturn(familyUser(2L, FamilyRelation.MOTHER, "엄마"));
        when(loadShopProductPort.loadById(20L)).thenReturn(Optional.of(product));
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(familyConnections.isConnected(1L, 2L)).thenReturn(true);
        when(saveShopProductPort.save(any(ShopProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShopProductResponse response = shopService.setPrice(2L, 20L, new SetShopProductPriceRequest(15, 9900));

        assertEquals(15, response.cloverPrice());
        assertEquals(9900, response.purchasePriceWon());
        assertEquals("https://shop.example/card", response.productUrl());
    }

    private LoadedUser childUser() {
        return new LoadedUser(1L, "child@test.com", "미아", com.wishdiary.domain.user.AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null);
    }

    private LoadedUser familyUser(Long userId, FamilyRelation relation, String label) {
        return new LoadedUser(userId, "family@test.com", label, com.wishdiary.domain.user.AppRole.FAMILY, userId + 10L, 100L, GroupMemberRole.FAMILY, relation, label);
    }

    private GiftRequestNotification pendingGiftRequest() {
        return GiftRequestNotification.reconstitute(
                100L,
                10L,
                "문구 세트",
                null,
                null,
                25,
                null,
                1L,
                "미아",
                2L,
                "엄마",
                "선물 받고 싶어요",
                GiftRequestStatus.PENDING,
                null,
                null,
                java.time.LocalDateTime.now()
        );
    }
}
