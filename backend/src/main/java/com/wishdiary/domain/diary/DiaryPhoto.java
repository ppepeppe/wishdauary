package com.wishdiary.domain.diary;

/** A validated JPEG attachment. Decoding and persistence belong to adapters. */
public final class DiaryPhoto {
    private final byte[] bytes;
    public DiaryPhoto(byte[] bytes) { this.bytes = bytes.clone(); }
    public byte[] bytes() { return bytes.clone(); }
}
