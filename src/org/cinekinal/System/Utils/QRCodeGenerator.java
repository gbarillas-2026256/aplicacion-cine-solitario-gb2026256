package org.cinekinal.system.utils;

import java.util.EnumMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Generador de códigos QR en formato JavaFX Image utilizando la librería ZXing.
 *
 * Características:
 * - Soporta caracteres especiales y tildes mediante codificación UTF-8.
 * - Margen compacto (margin = 1) para aprovechar todo el área del ImageView.
 * - Nivel de corrección de errores M (aprox. 15% de recuperación).
 * - Renderizado directo en WritableImage pixel por pixel, sin requerir Swing ni AWT.
 */
public class QRCodeGenerator {

    private QRCodeGenerator() {
    }

    /**
     * Generates a QR code image from arbitrary text content using ZXing.
     *
     * @param content Text to encode in the QR code.
     * @param size    Width and height in pixels of the resulting image.
     * @return An {@link Image} instance with the generated QR, or {@code null} on failure.
     */
    public static Image generate(String content, int size) {
        if (content == null || content.isBlank() || size <= 0) {
            return null;
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int width = matrix.getWidth();
            int height = matrix.getHeight();

            WritableImage image = new WritableImage(width, height);
            PixelWriter writerPixel = image.getPixelWriter();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writerPixel.setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return image;
        } catch (Exception e) {
            System.err.println("Error al generar el código QR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Image generar(String contenido, int tamano) {
        return generate(contenido, tamano);
    }
}
