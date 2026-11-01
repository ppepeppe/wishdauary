package com.wishdiary.application.shop;
import java.util.List;
public interface ManageFamilyShopUseCase {
    List<ShopProductResponse> list(Long familyId);
    List<ShopProductResponse> listManaged(Long familyId);
    ShopProductResponse create(Long familyId, CreateFamilyShopProductRequest request);
}
