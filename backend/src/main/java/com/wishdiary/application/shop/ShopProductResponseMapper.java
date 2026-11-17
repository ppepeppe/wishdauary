package com.wishdiary.application.shop;

import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductSource;
import com.wishdiary.application.user.LoadUserPort;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ShopProductResponseMapper {
    private final LoadUserPort users;
    public ShopProductResponseMapper(LoadUserPort users) { this.users = users; }

    private String displayName(Long id) {
        if (id == null) return null;
        var user = users.loadByIds(List.of(id)).get(id);
        if (user == null) return null;
        return user.relationLabel() == null || user.relationLabel().isBlank() || user.relationLabel().equals(user.nickname())
                ? user.nickname() : user.nickname() + " (" + user.relationLabel() + ")";
    }


    public ShopProductResponse toResponse(ShopProduct product) {
        return new ShopProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                ShopPhotoResponse.imageUrl(product.getPhoto(), product.getImageUrl()),
                product.getProductUrl(),
                product.getCloverPrice(),
                product.getPurchasePriceWon(),
                product.getSource().name().toLowerCase(),
                product.getChildUserId(),
                product.getCreatedAt(),
                product.getReceiverFamilyUserId(),
                displayName(product.getCreatedByUserId() != null ? product.getCreatedByUserId()
                        : product.getSource() == ShopProductSource.CUSTOM ? product.getChildUserId() : null),
                displayName(product.getReceiverFamilyUserId())
        );
    }
}
