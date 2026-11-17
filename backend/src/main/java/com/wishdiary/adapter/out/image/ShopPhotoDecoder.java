package com.wishdiary.adapter.out.image;

import com.wishdiary.application.shop.DecodeShopPhotoPort;
import com.wishdiary.application.shop.InvalidShopPhotoException;
import com.wishdiary.domain.shop.ShopPhoto;
import org.springframework.stereotype.Component;

@Component
public class ShopPhotoDecoder implements DecodeShopPhotoPort {
    private final JpegPhotoDecoder decoder = new JpegPhotoDecoder();

    @Override
    public ShopPhoto decode(String data) {
        try {
            byte[] bytes = decoder.decode(data);
            return bytes == null ? null : new ShopPhoto(bytes);
        } catch (InvalidPhotoDataException error) {
            throw new InvalidShopPhotoException(error.getMessage());
        }
    }
}
