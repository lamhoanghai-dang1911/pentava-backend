package com.nqt.shop.usecase.boundary;

import java.util.List;

public interface SkinServiceGateway {
    List<Long> getUserOwnedSkinIds(Long userId);
    boolean checkSkinOwned(Long userId, Long skinItemId);
    boolean grantSkin(Long userId, Long skinItemId);
}
