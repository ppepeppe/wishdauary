package com.wishdiary.adapter.out.persistence.shop;

import com.wishdiary.application.shop.LoadGiftRequestNotificationPort;
import com.wishdiary.application.shop.LoadShopProductPort;
import com.wishdiary.application.shop.SaveGiftRequestNotificationPort;
import com.wishdiary.application.shop.SaveShopProductPort;
import com.wishdiary.domain.notification.GiftRequestNotification;
import com.wishdiary.domain.notification.GiftRequestStatus;
import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopPhoto;
import com.wishdiary.domain.shop.ShopProductSource;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ShopPersistenceAdapter implements LoadShopProductPort, SaveShopProductPort, SaveGiftRequestNotificationPort, LoadGiftRequestNotificationPort {

    private final ShopProductJpaRepository shopProductJpaRepository;
    private final GiftRequestNotificationJpaRepository giftRequestNotificationJpaRepository;

    public ShopPersistenceAdapter(
            ShopProductJpaRepository shopProductJpaRepository,
            GiftRequestNotificationJpaRepository giftRequestNotificationJpaRepository
    ) {
        this.shopProductJpaRepository = shopProductJpaRepository;
        this.giftRequestNotificationJpaRepository = giftRequestNotificationJpaRepository;
    }

    @Override
    public List<ShopProduct> loadAll() {
        return shopProductJpaRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ShopProductJpaEntity::getId))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ShopProduct> loadById(Long productId) {
        return shopProductJpaRepository.findById(productId).map(this::toDomain);
    }

    @Override
    public ShopProduct save(ShopProduct shopProduct) {
        return toDomain(shopProductJpaRepository.save(toEntity(shopProduct)));
    }

    @Override
    public GiftRequestNotification save(GiftRequestNotification notification) {
        return toDomain(giftRequestNotificationJpaRepository.save(toEntity(notification)));
    }

    @Override
    public Optional<GiftRequestNotification> loadGiftRequestById(Long requestId) {
        return giftRequestNotificationJpaRepository.findById(requestId).map(this::toDomain);
    }

    @Override
    public List<GiftRequestNotification> loadAllByReceiverFamilyUserId(Long familyUserId) {
        return giftRequestNotificationJpaRepository.findAllByReceiverFamilyUserIdOrderByCreatedAtDesc(familyUserId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<GiftRequestNotification> loadAllByRequesterChildUserId(Long childUserId) {
        return giftRequestNotificationJpaRepository.findAllByRequesterChildUserIdOrderByCreatedAtDesc(childUserId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ShopProductJpaEntity toEntity(ShopProduct product) {
        return new ShopProductJpaEntity(
                product.getId(),
                product.getChildUserId(),
                product.getTitle(),
                product.getDescription(),
                product.getImageUrl(),
                product.getProductUrl(),
                product.getCloverPrice(),
                product.getPurchasePriceWon(),
                product.getSource(),
                product.getCreatedAt(),
                product.getReceiverFamilyUserId()
        ).withImageData(product.getPhoto() == null ? null : product.getPhoto().bytes());
    }

    private ShopProduct toDomain(ShopProductJpaEntity entity) {
        return ShopProduct.reconstitute(
                entity.getId(),
                entity.getChildUserId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getProductUrl(),
                entity.getCloverPrice(),
                entity.getPurchasePriceWon(),
                entity.getSource(),
                entity.getCreatedAt(),
                entity.getReceiverFamilyUserId()
        ).withPhoto(entity.getImageData() == null ? null : new ShopPhoto(entity.getImageData()));
    }

    private GiftRequestNotificationJpaEntity toEntity(GiftRequestNotification notification) {
        return new GiftRequestNotificationJpaEntity(
                notification.getId(),
                notification.getProductId(),
                notification.getProductTitle(),
                notification.getProductImageUrl(),
                notification.getProductUrl(),
                notification.getCloverPrice(),
                notification.getPurchasePriceWon(),
                notification.getRequesterChildUserId(),
                notification.getRequesterChildNickname(),
                notification.getReceiverFamilyUserId(),
                notification.getReceiverRelationLabel(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getProcessedByFamilyUserId(),
                notification.getProcessedAt(),
                notification.getCreatedAt()
        ).withImageData(notification.getPhoto() == null ? null : notification.getPhoto().bytes());
    }

    private GiftRequestNotification toDomain(GiftRequestNotificationJpaEntity entity) {
        return GiftRequestNotification.reconstitute(
                entity.getId(),
                entity.getProductId(),
                entity.getProductTitle(),
                entity.getProductImageUrl(),
                entity.getProductUrl(),
                entity.getCloverPrice(),
                entity.getPurchasePriceWon(),
                entity.getRequesterChildUserId(),
                entity.getRequesterChildNickname(),
                entity.getReceiverFamilyUserId(),
                entity.getReceiverRelationLabel(),
                entity.getMessage(),
                entity.getStatus() == null ? GiftRequestStatus.PENDING : entity.getStatus(),
                entity.getProcessedByFamilyUserId(),
                entity.getProcessedAt(),
                entity.getCreatedAt()
        ).withPhoto(entity.getImageData() == null ? null : new ShopPhoto(entity.getImageData()));
    }
}
