package com.wishdiary.domain.shop;

/** A validated JPEG photo. Validation and conversion belong to the image adapter. */
public final class ShopPhoto {
    private final byte[] bytes;

    public ShopPhoto(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] bytes() {
        return bytes.clone();
    }
}
