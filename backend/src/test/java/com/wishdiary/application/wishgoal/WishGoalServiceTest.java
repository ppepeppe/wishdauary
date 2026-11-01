package com.wishdiary.application.wishgoal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wishdiary.application.clover.LoadCloverBalancePort;
import com.wishdiary.application.shop.LoadShopProductPort;
import com.wishdiary.application.user.LoadUserPort;
import com.wishdiary.application.group.FamilyConnectionPort;
import com.wishdiary.application.user.LoadedUser;
import com.wishdiary.domain.group.GroupMemberRole;
import com.wishdiary.domain.shop.ChildOnlyGiftRequestException;
import com.wishdiary.domain.shop.ShopProduct;
import com.wishdiary.domain.shop.ShopProductNotFoundException;
import com.wishdiary.domain.shop.ShopProductPriceNotSetException;
import com.wishdiary.domain.shop.ShopProductSource;
import com.wishdiary.domain.user.AppRole;
import com.wishdiary.domain.wishgoal.WishGoal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WishGoalServiceTest {

    @Mock
    private LoadWishGoalPort loadWishGoalPort;

    @Mock
    private SaveWishGoalPort saveWishGoalPort;

    @Mock
    private LoadShopProductPort loadShopProductPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private FamilyConnectionPort familyConnections;

    @Mock
    private LoadCloverBalancePort loadCloverBalancePort;

    private WishGoalService wishGoalService;

    @BeforeEach
    void setUp() {
        wishGoalService = new WishGoalService(
                loadWishGoalPort,
                saveWishGoalPort,
                loadShopProductPort,
                loadUserPort,
                familyConnections,
                loadCloverBalancePort
        );
    }

    @Test
    void setGoalSavesGoalForChild() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.of(presetProduct(10L, "레고 시티 세트", 50)));
        when(loadCloverBalancePort.loadBalance(1L)).thenReturn(12);
        when(saveWishGoalPort.save(any(WishGoal.class))).thenAnswer(invocation -> {
            WishGoal goal = invocation.getArgument(0);
            return WishGoal.reconstitute(77L, goal.getChildUserId(), goal.getProductId(), goal.getCreatedAt());
        });

        WishGoalResponse response = wishGoalService.setGoal(1L, new SetWishGoalRequest(10L));

        assertEquals(77L, response.goalId());
        assertEquals(10L, response.productId());
        assertEquals("레고 시티 세트", response.productTitle());
        assertEquals(50, response.targetCloverPrice());
        assertEquals(12, response.currentCloverBalance());
        assertEquals(38, response.remainingClovers());

        ArgumentCaptor<WishGoal> goalCaptor = ArgumentCaptor.forClass(WishGoal.class);
        verify(saveWishGoalPort).save(goalCaptor.capture());
        assertEquals(1L, goalCaptor.getValue().getChildUserId());
        assertEquals(10L, goalCaptor.getValue().getProductId());
    }

    @Test
    void setGoalFailsWhenUserIsNotChild() {
        when(loadUserPort.loadById(2L)).thenReturn(familyUser());

        assertThrows(
                ChildOnlyGiftRequestException.class,
                () -> wishGoalService.setGoal(2L, new SetWishGoalRequest(10L))
        );
    }

    @Test
    void setGoalFailsWhenProductNotFound() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadShopProductPort.loadById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ShopProductNotFoundException.class,
                () -> wishGoalService.setGoal(1L, new SetWishGoalRequest(99L))
        );
    }

    @Test
    void setGoalFailsWhenProductPriceNotSet() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.of(customProductWithoutPrice(10L)));

        assertThrows(
                ShopProductPriceNotSetException.class,
                () -> wishGoalService.setGoal(1L, new SetWishGoalRequest(10L))
        );
    }

    @Test
    void getGoalReturnsProgress() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadWishGoalPort.loadByChildUserId(1L)).thenReturn(Optional.of(
                WishGoal.reconstitute(77L, 1L, 10L, LocalDateTime.of(2026, 7, 1, 9, 0))
        ));
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.of(presetProduct(10L, "레고 시티 세트", 50)));
        when(loadCloverBalancePort.loadBalance(1L)).thenReturn(60);

        WishGoalResponse response = wishGoalService.getGoal(1L);

        assertEquals(77L, response.goalId());
        assertEquals(50, response.targetCloverPrice());
        assertEquals(60, response.currentCloverBalance());
        assertEquals(0, response.remainingClovers());
    }

    @Test
    void getGoalReturnsNullWhenNoGoal() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadWishGoalPort.loadByChildUserId(1L)).thenReturn(Optional.empty());

        assertNull(wishGoalService.getGoal(1L));
    }

    @Test
    void getGoalReturnsNullWhenGoalProductWasDeleted() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());
        when(loadWishGoalPort.loadByChildUserId(1L)).thenReturn(Optional.of(
                WishGoal.reconstitute(77L, 1L, 10L, LocalDateTime.of(2026, 7, 1, 9, 0))
        ));
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.empty());

        assertNull(wishGoalService.getGoal(1L));
    }

    @Test
    void clearGoalDeletesGoal() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());

        wishGoalService.clearGoal(1L);

        verify(saveWishGoalPort).deleteByChildUserId(1L);
    }

    @Test
    void getChildrenGoalsReturnsGoalsOfChildrenInFamilyGroup() {
        when(loadUserPort.loadById(2L)).thenReturn(familyUser());
        when(familyConnections.findChildUserIds(2L)).thenReturn(java.util.List.of(1L));
        when(loadUserPort.loadByIds(java.util.List.of(1L))).thenReturn(java.util.Map.of(1L, childUser()));
        when(loadWishGoalPort.loadByChildUserId(1L)).thenReturn(Optional.of(
                WishGoal.reconstitute(77L, 1L, 10L, LocalDateTime.of(2026, 7, 1, 9, 0))
        ));
        when(loadShopProductPort.loadById(10L)).thenReturn(Optional.of(presetProduct(10L, "레고 시티 세트", 50)));
        when(loadCloverBalancePort.loadBalance(1L)).thenReturn(20);

        java.util.List<ChildWishGoalResponse> responses = wishGoalService.getChildrenGoals(2L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).childUserId());
        assertEquals("미아", responses.get(0).childNickname());
        assertEquals("레고 시티 세트", responses.get(0).goal().productTitle());
        assertEquals(30, responses.get(0).goal().remainingClovers());
    }

    @Test
    void getChildrenGoalsSkipsChildrenWithoutGoal() {
        when(loadUserPort.loadById(2L)).thenReturn(familyUser());
        when(familyConnections.findChildUserIds(2L)).thenReturn(java.util.List.of(1L));
        when(loadUserPort.loadByIds(java.util.List.of(1L))).thenReturn(java.util.Map.of(1L, childUser()));
        when(loadWishGoalPort.loadByChildUserId(1L)).thenReturn(Optional.empty());

        java.util.List<ChildWishGoalResponse> responses = wishGoalService.getChildrenGoals(2L);

        assertEquals(0, responses.size());
    }

    @Test
    void getChildrenGoalsFailsWhenUserIsNotFamily() {
        when(loadUserPort.loadById(1L)).thenReturn(childUser());

        assertThrows(
                com.wishdiary.domain.shop.GiftRequestReceiverNotAllowedException.class,
                () -> wishGoalService.getChildrenGoals(1L)
        );
    }

    private LoadedUser childUser() {
        return new LoadedUser(1L, "child@test.com", "미아", AppRole.CHILD, 11L, 100L, GroupMemberRole.CHILD, null, null);
    }

    private LoadedUser familyUser() {
        return new LoadedUser(2L, "family@test.com", "엄마", AppRole.FAMILY, 12L, 100L, GroupMemberRole.FAMILY, null, "엄마");
    }

    private ShopProduct presetProduct(Long productId, String title, int cloverPrice) {
        return ShopProduct.reconstitute(productId, null, title, "설명", null, null, cloverPrice, null, ShopProductSource.PRESET, LocalDateTime.now());
    }

    private ShopProduct customProductWithoutPrice(Long productId) {
        return ShopProduct.reconstitute(productId, 1L, "카드", "포켓몬 카드", null, "https://shop.example/card", 0, null, ShopProductSource.CUSTOM, LocalDateTime.now());
    }
}
