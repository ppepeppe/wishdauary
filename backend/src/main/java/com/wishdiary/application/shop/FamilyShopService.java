package com.wishdiary.application.shop;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.sharing.SharingGroupPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.domain.group.FamilyConnectionAccessDeniedException;
import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductSource;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FamilyShopService implements ManageFamilyShopUseCase {
    private final DecodeShopPhotoPort photos;
    private final LoadUserPort users;
    private final FamilyConnectionPort connections;
    private final SharingGroupPort groups;
    private final LoadShopProductPort products;
    private final SaveShopProductPort saveProducts;
    private final ShopProductResponseMapper mapper;
    public FamilyShopService(LoadUserPort users, FamilyConnectionPort connections, SharingGroupPort groups,
            LoadShopProductPort products, SaveShopProductPort saveProducts, ShopProductResponseMapper mapper, DecodeShopPhotoPort photos) {
        this.photos = photos;
        this.users = users; this.connections = connections; this.groups = groups;
        this.products = products; this.saveProducts = saveProducts; this.mapper = mapper;
    }
    public List<ShopProductResponse> list(Long familyId) {
        requireFamily(familyId);
        List<Long> children = connections.findChildUserIds(familyId);
        return products.loadAll().stream().filter(product -> product.getSource() == ShopProductSource.FAMILY)
                .filter(product -> children.contains(product.getChildUserId()))
                .filter(product -> product.mayBePricedBy(familyId))
                .sorted(Comparator.comparing(ShopProduct::getCreatedAt).reversed()).map(mapper::toResponse).toList();
    }
    public List<ShopProductResponse> listManaged(Long familyId) {
        requireFamily(familyId);
        List<Long> children = connections.findChildUserIds(familyId);
        return products.loadAll().stream()
                .filter(product -> product.getSource() == ShopProductSource.CUSTOM || product.getSource() == ShopProductSource.FAMILY)
                .filter(product -> children.contains(product.getChildUserId()))
                .filter(product -> product.mayBePricedBy(familyId))
                .sorted(Comparator.comparing(ShopProduct::getCreatedAt).reversed())
                .map(mapper::toResponse).toList();
    }
    @Transactional
    public ShopProductResponse create(Long familyId, CreateFamilyShopProductRequest request) {
        requireFamily(familyId);
        groups.lockOwner(request.childUserId());
        if (!users.loadById(request.childUserId()).isChild() || !connections.isConnected(request.childUserId(), familyId)) {
            throw new FamilyConnectionAccessDeniedException();
        }
        return mapper.toResponse(saveProducts.save(ShopProduct.createFamily(request.childUserId(), request.title().trim(),
                request.description().trim(), request.imageUrl(), request.productUrl(), request.cloverPrice(), request.purchasePriceWon())
                .withPhoto(photos.decode(request.imageData()))));
    }
    private void requireFamily(Long familyId) {
        if (!users.loadById(familyId).isFamily()) throw new FamilyConnectionAccessDeniedException();
    }
}
