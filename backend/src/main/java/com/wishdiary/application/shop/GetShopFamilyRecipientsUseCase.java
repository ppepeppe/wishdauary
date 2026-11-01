package com.wishdiary.application.shop;

import java.util.List;

public interface GetShopFamilyRecipientsUseCase {

    List<ShopFamilyRecipientResponse> getRecipients(Long childUserId);
}
