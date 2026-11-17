package com.wishdiary.adapter.out.image;

import com.wishdiary.application.diary.DecodeDiaryPhotoPort;
import com.wishdiary.application.diary.InvalidDiaryPhotoException;
import com.wishdiary.domain.diary.DiaryPhoto;
import org.springframework.stereotype.Component;

@Component
public class DiaryPhotoDecoder implements DecodeDiaryPhotoPort {
    private final JpegPhotoDecoder decoder = new JpegPhotoDecoder();

    public DiaryPhoto decode(String data) {
        try {
            byte[] bytes = decoder.decode(data);
            return bytes == null ? null : new DiaryPhoto(bytes);
        } catch (InvalidPhotoDataException error) {
            throw new InvalidDiaryPhotoException(error.getMessage());
        }
    }
}
