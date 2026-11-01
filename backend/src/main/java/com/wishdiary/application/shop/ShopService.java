package com.wishdiary.application.shop;

import com.wishdiary.application.clover.LoadCloverBalancePort;
import com.wishdiary.application.clover.RecordCloverLedgerPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.clover.CloverLedger;
import com.wishdiary.domain.clover.CloverReason;
import com.wishdiary.domain.notification.GiftRequestNotification;
import com.wishdiary.domain.notification.GiftRequestStatus;
import com.wishdiary.domain.shop.ChildOnlyGiftRequestException;
import com.wishdiary.domain.shop.GiftRequestReceiverNotAllowedException;
import com.wishdiary.domain.shop.GiftRequestNotFoundException;
import com.wishdiary.domain.shop.InsufficientCloverBalanceException;
import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductNotFoundException;
import com.wishdiary.domain.shop.ShopProductPriceNotSetException;
import com.wishdiary.domain.shop.ShopProductPriceSettingNotAllowedException;
import com.wishdiary.domain.shop.ShopProductSource;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService implements GetShopProductsUseCase, GetShopFamilyRecipientsUseCase, RequestGiftUseCase, GetGiftRequestsUseCase, GetSentGiftRequestsUseCase, CreateShopProductUseCase, ProcessGiftRequestUseCase, GetShopProductPricingRequestsUseCase, SetShopProductPriceUseCase {

    private final LoadShopProductPort loadShopProductPort;
    private final SaveShopProductPort saveShopProductPort;
    private final SaveGiftRequestNotificationPort saveGiftRequestNotificationPort;
    private final LoadGiftRequestNotificationPort loadGiftRequestNotificationPort;
    private final LoadUserPort loadUserPort;
    private final FamilyConnectionPort familyConnections;
    private final LoadCloverBalancePort loadCloverBalancePort;
    private final RecordCloverLedgerPort recordCloverLedgerPort;
    private final ShopProductResponseMapper shopProductResponseMapper;
    private final GiftRequestResponseMapper giftRequestResponseMapper;
    private final GiftRequestNotificationResponseMapper giftRequestNotificationResponseMapper;

    public ShopService(
            LoadShopProductPort loadShopProductPort,
            SaveShopProductPort saveShopProductPort,
            SaveGiftRequestNotificationPort saveGiftRequestNotificationPort,
            LoadGiftRequestNotificationPort loadGiftRequestNotificationPort,
            LoadUserPort loadUserPort,
            FamilyConnectionPort familyConnections,
            LoadCloverBalancePort loadCloverBalancePort,
            RecordCloverLedgerPort recordCloverLedgerPort,
            ShopProductResponseMapper shopProductResponseMapper,
            GiftRequestResponseMapper giftRequestResponseMapper,
            GiftRequestNotificationResponseMapper giftRequestNotificationResponseMapper
    ) {
        this.loadShopProductPort = loadShopProductPort;
        this.saveShopProductPort = saveShopProductPort;
        this.saveGiftRequestNotificationPort = saveGiftRequestNotificationPort;
        this.loadGiftRequestNotificationPort = loadGiftRequestNotificationPort;
        this.loadUserPort = loadUserPort;
        this.familyConnections = familyConnections;
        this.loadCloverBalancePort = loadCloverBalancePort;
        this.recordCloverLedgerPort = recordCloverLedgerPort;
        this.shopProductResponseMapper = shopProductResponseMapper;
        this.giftRequestResponseMapper = giftRequestResponseMapper;
        this.giftRequestNotificationResponseMapper = giftRequestNotificationResponseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopProductResponse> getProducts(Long childUserId) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);
        return loadShopProductPort.loadAll()
                .stream()
                .filter(product -> product.isAvailableTo(childUserId))
                .sorted(Comparator.comparingInt(ShopProduct::getCloverPrice).thenComparing(ShopProduct::getId))
                .map(shopProductResponseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ShopProductResponse create(Long childUserId, CreateShopProductRequest request) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        ShopProduct savedProduct = saveShopProductPort.save(ShopProduct.createCustom(
                childUser.id(),
                request.title(),
                request.description(),
                request.imageUrl(),
                request.productUrl(),
                request.cloverPrice() == null ? 0 : request.cloverPrice()
        ));
        return shopProductResponseMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopFamilyRecipientResponse> getRecipients(Long childUserId) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        return loadUserPort.loadByIds(familyConnections.findFamilyUserIds(childUserId)).values()
                .stream()
                .sorted(Comparator.comparing(LoadedUser::id))
                .map(user -> new ShopFamilyRecipientResponse(user.id(), user.nickname(), user.relationLabel()))
                .toList();
    }

    @Override
    @Transactional
    public GiftRequestResponse request(Long childUserId, RequestGiftRequest request) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        ShopProduct product = loadShopProductPort.loadById(request.productId())
                .filter(candidate -> candidate.isAvailableTo(childUserId))
                .orElseThrow(() -> new ShopProductNotFoundException(request.productId()));
        if (product.getCloverPrice() <= 0) {
            throw new ShopProductPriceNotSetException();
        }
        LoadedUser receiver = loadUserPort.loadById(request.receiverFamilyUserId());
        validateReceiver(childUser, receiver);

        int currentBalance = loadCloverBalancePort.loadBalance(childUserId);
        if (currentBalance < product.getCloverPrice()) {
            throw new InsufficientCloverBalanceException();
        }

        GiftRequestNotification notification = saveGiftRequestNotificationPort.save(GiftRequestNotification.create(
                product.getId(),
                product.getTitle(),
                product.getImageUrl(),
                product.getProductUrl(),
                product.getCloverPrice(),
                product.getPurchasePriceWon(),
                childUser.id(),
                childUser.nickname(),
                receiver.id(),
                receiver.relationLabel(),
                request.message()
        ));
        recordCloverLedgerPort.record(CloverLedger.create(
                childUser.id(),
                -product.getCloverPrice(),
                CloverReason.GIFT_REQUEST,
                notification.getId()
        ));
        int remainingBalance = currentBalance - product.getCloverPrice();
        return giftRequestResponseMapper.toResponse(notification, product.getCloverPrice(), remainingBalance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiftRequestNotificationResponse> getReceivedRequests(Long familyUserId) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        if (!familyUser.isFamily()) {
            throw new GiftRequestReceiverNotAllowedException();
        }

        return giftRequestNotificationResponseMapper.toResponses(
                loadGiftRequestNotificationPort.loadAllByReceiverFamilyUserId(familyUserId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiftRequestNotificationResponse> getSentRequests(Long childUserId) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        return giftRequestNotificationResponseMapper.toResponses(
                loadGiftRequestNotificationPort.loadAllByRequesterChildUserId(childUserId)
        );
    }

    @Override
    @Transactional
    public GiftRequestNotificationResponse process(Long familyUserId, Long requestId, ProcessGiftRequestRequest request) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        if (!familyUser.isFamily()) {
            throw new GiftRequestReceiverNotAllowedException();
        }

        GiftRequestNotification notification = loadGiftRequestNotificationPort.loadGiftRequestById(requestId)
                .orElseThrow(() -> new GiftRequestNotFoundException(requestId));
        if (!familyUserId.equals(notification.getReceiverFamilyUserId())) {
            throw new GiftRequestReceiverNotAllowedException();
        }

        GiftRequestNotification processed = switch (request.action()) {
            case APPROVE -> notification.approve(familyUserId);
            case REJECT -> notification.reject(familyUserId);
        };
        GiftRequestNotification saved = saveGiftRequestNotificationPort.save(processed);
        refundCloverIfRejected(saved);
        return giftRequestNotificationResponseMapper.toResponse(saved);
    }

    private void refundCloverIfRejected(GiftRequestNotification notification) {
        if (notification.getStatus() != GiftRequestStatus.REJECTED) {
            return;
        }
        boolean alreadyRefunded = recordCloverLedgerPort.existsByUserIdAndReasonAndRefId(
                notification.getRequesterChildUserId(),
                CloverReason.GIFT_REQUEST_REFUND,
                notification.getId()
        );
        if (alreadyRefunded) {
            return;
        }
        recordCloverLedgerPort.record(CloverLedger.create(
                notification.getRequesterChildUserId(),
                notification.getCloverPrice(),
                CloverReason.GIFT_REQUEST_REFUND,
                notification.getId()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopProductResponse> getPricingRequests(Long familyUserId) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        if (!familyUser.isFamily()) {
            throw new ShopProductPriceSettingNotAllowedException(null);
        }
        List<Long> childUserIds = familyConnections.findChildUserIds(familyUserId);

        return loadShopProductPort.loadAll()
                .stream()
                .filter(product -> product.getSource() == ShopProductSource.CUSTOM)
                .filter(product -> product.getCloverPrice() <= 0)
                .filter(product -> childUserIds.contains(product.getChildUserId()))
                .sorted(Comparator.comparing(ShopProduct::getCreatedAt).reversed())
                .map(shopProductResponseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ShopProductResponse setPrice(Long familyUserId, Long productId, SetShopProductPriceRequest request) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        if (!familyUser.isFamily()) {
            throw new ShopProductPriceSettingNotAllowedException(productId);
        }

        ShopProduct product = loadShopProductPort.loadById(productId)
                .orElseThrow(() -> new ShopProductNotFoundException(productId));
        if (product.getSource() == ShopProductSource.PRESET || product.getChildUserId() == null) {
            throw new ShopProductPriceSettingNotAllowedException(productId);
        }
        LoadedUser childUser = loadUserPort.loadById(product.getChildUserId());
        if (!familyConnections.isConnected(childUser.id(), familyUserId)) {
            throw new ShopProductPriceSettingNotAllowedException(productId);
        }

        return shopProductResponseMapper.toResponse(saveShopProductPort.save(product.price(
                request.cloverPrice(),
                request.purchasePriceWon()
        )));
    }

    private void validateChildUser(LoadedUser user) {
        if (!user.isChild()) {
            throw new ChildOnlyGiftRequestException();
        }
    }

    private void validateReceiver(LoadedUser childUser, LoadedUser receiver) {
        if (!receiver.isFamily() || !familyConnections.isConnected(childUser.id(), receiver.id())) {
            throw new GiftRequestReceiverNotAllowedException();
        }
    }
}
