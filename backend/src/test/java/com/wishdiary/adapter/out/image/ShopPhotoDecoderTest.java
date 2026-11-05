package com.wishdiary.adapter.out.image;

import static org.junit.jupiter.api.Assertions.*;

import com.wishdiary.application.shop.InvalidShopPhotoException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ShopPhotoDecoderTest {
    private final ShopPhotoDecoder decoder = new ShopPhotoDecoder();

    @Test
    void decodesRealPhotosIntoJpegWithoutExposingMutableBytes() throws Exception {
        byte[] bytes = decoder.decode(image(20, 10)).bytes();
        assertEquals("jpeg", ImageIO.getImageReaders(ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))).next().getFormatName().toLowerCase());
        assertEquals(20, ImageIO.read(new ByteArrayInputStream(bytes)).getWidth());
        var photo = decoder.decode(image(20, 10));
        byte first = photo.bytes()[0];
        photo.bytes()[0] = 0;
        assertEquals(first, photo.bytes()[0]);
        assertNull(decoder.decode(null));
        assertNull(decoder.decode(""));
    }

    @Test
    void rejectsNonImagesMalformedDataOversizedFilesAndExcessiveDimensions() throws Exception {
        for (String data : new String[] {"https://example.com/a.jpg", "data:image/jpeg;base64,%%%",
                "data:image/png;base64,aGVsbG8=", "data:image/svg+xml;base64,PHN2Zy8+",
                "data:image/jpeg;base64," + "A".repeat(700_000), image(4097, 1)}) {
            assertThrows(InvalidShopPhotoException.class, () -> decoder.decode(data));
        }
    }

    private String image(int width, int height) throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", output);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
    }
}
