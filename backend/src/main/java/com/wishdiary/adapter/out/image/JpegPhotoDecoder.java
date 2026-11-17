package com.wishdiary.adapter.out.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

final class JpegPhotoDecoder {
    private static final int MAX_BYTES = 512_000;

    public byte[] decode(String data) {
        if (data == null || data.isBlank()) return null;
        if (data.length() > 700_000) throw new InvalidPhotoDataException("사진 용량이 커요. 더 작은 사진을 선택해 주세요.");
        if (!data.startsWith("data:image/jpeg;base64,") && !data.startsWith("data:image/png;base64,")) {
            throw new InvalidPhotoDataException();
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(data.substring(data.indexOf(',') + 1));
            if (bytes.length == 0 || bytes.length > MAX_BYTES) throw new InvalidPhotoDataException();
            return read(bytes);
        } catch (IOException | IllegalArgumentException exception) {
            throw new InvalidPhotoDataException();
        }
    }

    private byte[] read(byte[] bytes) throws IOException {
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new InvalidPhotoDataException();
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("jpeg") && !format.equalsIgnoreCase("png")) throw new InvalidPhotoDataException();
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 4096 || height > 4096 || (long) width * height > 4_000_000) {
                    throw new InvalidPhotoDataException("사진 크기가 커요. 더 작은 사진을 선택해 주세요.");
                }
                return jpeg(reader.read(0));
            } finally {
                reader.dispose();
            }
        }
    }

    private byte[] jpeg(BufferedImage source) throws IOException {
        var image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", output);
        if (output.size() > MAX_BYTES) throw new InvalidPhotoDataException("사진 용량이 커요. 더 작은 사진을 선택해 주세요.");
        return output.toByteArray();
    }
}
