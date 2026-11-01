package com.wishdiary.application.shop;

import java.util.List;

public interface GetShopProductPricingRequestsUseCase {

    List<ShopProductResponse> getPricingRequests(Long familyUserId);
}
