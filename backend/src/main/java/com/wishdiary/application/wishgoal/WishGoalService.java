package com.wishdiary.application.wishgoal;

import com.wishdiary.application.clover.LoadCloverBalancePort;
import com.wishdiary.application.shop.LoadShopProductPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.shop.ChildOnlyGiftRequestException;
import com.wishdiary.domain.shop.GiftRequestReceiverNotAllowedException;
import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductNotFoundException;
import com.wishdiary.domain.shop.ShopProductPriceNotSetException;
import com.wishdiary.domain.wishgoal.WishGoal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishGoalService implements SetWishGoalUseCase, GetWishGoalUseCase, ClearWishGoalUseCase, GetChildrenWishGoalsUseCase {

    private final LoadWishGoalPort loadWishGoalPort;
    private final SaveWishGoalPort saveWishGoalPort;
    private final LoadShopProductPort loadShopProductPort;
    private final LoadUserPort loadUserPort;
    private final FamilyConnectionPort familyConnections;
    private final LoadCloverBalancePort loadCloverBalancePort;

    public WishGoalService(
            LoadWishGoalPort loadWishGoalPort,
            SaveWishGoalPort saveWishGoalPort,
            LoadShopProductPort loadShopProductPort,
            LoadUserPort loadUserPort,
            FamilyConnectionPort familyConnections,
            LoadCloverBalancePort loadCloverBalancePort
    ) {
        this.loadWishGoalPort = loadWishGoalPort;
        this.saveWishGoalPort = saveWishGoalPort;
        this.loadShopProductPort = loadShopProductPort;
        this.loadUserPort = loadUserPort;
        this.familyConnections = familyConnections;
        this.loadCloverBalancePort = loadCloverBalancePort;
    }

    @Override
    @Transactional
    public WishGoalResponse setGoal(Long childUserId, SetWishGoalRequest request) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        ShopProduct product = loadShopProductPort.loadById(request.productId())
                .orElseThrow(() -> new ShopProductNotFoundException(request.productId()));
        if (product.getCloverPrice() <= 0) {
            throw new ShopProductPriceNotSetException();
        }

        saveWishGoalPort.deleteByChildUserId(childUserId);
        WishGoal savedGoal = saveWishGoalPort.save(WishGoal.create(childUserId, product.getId()));
        return toResponse(savedGoal, product, loadCloverBalancePort.loadBalance(childUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public WishGoalResponse getGoal(Long childUserId) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        Optional<WishGoal> goal = loadWishGoalPort.loadByChildUserId(childUserId);
        if (goal.isEmpty()) {
            return null;
        }

        Optional<ShopProduct> product = loadShopProductPort.loadById(goal.get().getProductId());
        if (product.isEmpty()) {
            return null;
        }

        return toResponse(goal.get(), product.get(), loadCloverBalancePort.loadBalance(childUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildWishGoalResponse> getChildrenGoals(Long familyUserId) {
        LoadedUser familyUser = loadUserPort.loadById(familyUserId);
        if (!familyUser.isFamily()) {
            throw new GiftRequestReceiverNotAllowedException();
        }

        return loadUserPort.loadByIds(familyConnections.findChildUserIds(familyUserId)).values()
                .stream()
                .map(this::toChildGoalResponse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<ChildWishGoalResponse> toChildGoalResponse(LoadedUser child) {
        return loadWishGoalPort.loadByChildUserId(child.id())
                .flatMap(goal -> loadShopProductPort.loadById(goal.getProductId())
                        .map(product -> new ChildWishGoalResponse(
                                child.id(),
                                child.nickname(),
                                toResponse(goal, product, loadCloverBalancePort.loadBalance(child.id()))
                        )));
    }

    @Override
    @Transactional
    public void clearGoal(Long childUserId) {
        LoadedUser childUser = loadUserPort.loadById(childUserId);
        validateChildUser(childUser);

        saveWishGoalPort.deleteByChildUserId(childUserId);
    }

    private WishGoalResponse toResponse(WishGoal goal, ShopProduct product, int currentBalance) {
        int remaining = Math.max(0, product.getCloverPrice() - currentBalance);
        return new WishGoalResponse(
                goal.getId(),
                product.getId(),
                product.getTitle(),
                product.getImageUrl(),
                product.getCloverPrice(),
                currentBalance,
                remaining,
                goal.getCreatedAt()
        );
    }

    private void validateChildUser(LoadedUser user) {
        if (!user.isChild()) {
            throw new ChildOnlyGiftRequestException();
        }
    }
}
