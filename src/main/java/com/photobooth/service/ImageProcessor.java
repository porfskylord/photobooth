package com.photobooth.service;

import com.photobooth.model.Photo;
import com.photobooth.model.Theme;
import com.photobooth.model.Theme.FilterType;
import com.photobooth.model.Theme.LayoutType;
import com.photobooth.model.Theme.TextOverlay;
import com.photobooth.util.ImageUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class ImageProcessor {

    private static final int PRINT_4X6_WIDTH = 1800;
    private static final int PRINT_4X6_HEIGHT = 1200;

    public static Image applyTheme(Photo photo, Theme theme){
        if (photo == null || theme == null){
            return null;
        }
        Image image = photo.getOriginalImage();

        if (theme.hasFilter()) {
            image = applyFilter(image, theme.getFilterType(), theme.getFilterIntensity());
        }

        if (theme.hasBorder()) {
            image = addBorder(image, theme);
        }

        image = addTextOverlays(image, theme);

        return image;

    }

    public static Image applyThemeWithLayout(List<Photo> photos, Theme theme){
        if (photos == null || photos.isEmpty() || theme == null){
            return null;
        }

        if (theme.getLayoutType() == LayoutType.SINGLE) {
            return applyTheme(photos.get(0), theme);
        }

        return composeLayout(photos, theme);
    }

    private static Image applyFilter(Image image, FilterType filterType, double intensity){
        return switch (filterType) {
            case GRAYSCALE ->  ImageUtils.applyGrayscale(image, intensity);
            case SEPIA -> ImageUtils.applySepia(image, intensity);
            case VIBRANT -> ImageUtils.applyVibrant(image, intensity);
            case WARM -> ImageUtils.applyWarmTone(image, intensity);
            case COOL -> ImageUtils.applyCoolTone(image, intensity);
            case CONTRAST -> ImageUtils.applyContrast(image, intensity);
            case BRIGHTNESS -> ImageUtils.applyBrightness(image, intensity);
            default -> image;
        };
    }

    private static Image addBorder(Image image, Theme theme){
        int borderWidth = theme.getBorderWidth();
        Color borderColor = theme.getBorderColor();
        Color bgColor = theme.getBackgroundColor();
        int cornerRadius = theme.getCornerRadius();

        BufferedImage original = SwingFXUtils.fromFXImage(image, null);

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        int newWidth = originalWidth + (borderWidth * 2);
        int newHeight = originalHeight + (borderWidth * 2);

        BufferedImage bordered = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bordered.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(toAwtColor(bgColor));
        g2d.fillRect(0, 0, newWidth, newHeight);

        g2d.setColor(toAwtColor(borderColor));
        if (cornerRadius > 0) {
            g2d.fillRoundRect(0, 0, newWidth, newHeight, cornerRadius * 2, cornerRadius * 2);
            g2d.setClip(new RoundRectangle2D.Float(
                    borderWidth, borderWidth, originalWidth, originalHeight,
                    cornerRadius, cornerRadius));
        } else {
            g2d.fillRect(0, 0, newWidth, newHeight);
        }

        g2d.drawImage(original, borderWidth, borderWidth, null);

        if (theme.hasShadow()) {
            addShadow(g2d, borderWidth, borderWidth, originalWidth, originalHeight);
        }

        g2d.dispose();

        return SwingFXUtils.toFXImage(bordered, null);
    }

    private static void addShadow(Graphics2D g2d, int x, int y, int width, int height){
        int shadowOffset = 5;
        int shadowBlur = 10;
        g2d.setColor(new java.awt.Color(0, 0, 0, 50));
        g2d.fillRect(x + shadowOffset, y + shadowOffset, width, height);
    }

    private static Image addTextOverlays(Image image, Theme theme){
        TextOverlay header = theme.getHeaderText();
        TextOverlay footer = theme.getFooterText();

        if (!header.isEnabled() && !footer.isEnabled()) {
            return image;
        }

        BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
        Graphics2D g2d = buffered.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int imageWidth = buffered.getWidth();
        int imageHeight = buffered.getHeight();

        if (footer.isEnabled() && !footer.getText().isEmpty()) {
            drawText(g2d, footer, imageWidth, imageHeight - 30);
        }

        g2d.dispose();

        return SwingFXUtils.toFXImage(buffered, null);
    }

    private static void drawText(Graphics2D g2d, TextOverlay textOverlay, int imageWidth, int yPosition){
        String text = textOverlay.getText();
        Font font = new Font(
                textOverlay.getFontFamily(),
                textOverlay.getFontWeight() == FontWeight.BOLD ? Font.BOLD : Font.PLAIN,
                textOverlay.getFontSize()
                );
        g2d.setFont(font);
        g2d.setColor(toAwtColor(textOverlay.getTextColor()));

        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int x = (imageWidth - textWidth) / 2;

        drawTextWithOutline(g2d, text, x, yPosition);
    }

    private static void drawTextWithOutline(Graphics2D g2d, String text, int x, int y){
        java.awt.Color originalColor = g2d.getColor();

        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawString(text, x - 1, y - 1);
        g2d.drawString(text, x + 1, y - 1);
        g2d.drawString(text, x - 1, y + 1);
        g2d.drawString(text, x + 1, y + 1);

        g2d.setColor(originalColor);
        g2d.drawString(text, x, y);
    }

    private static Image composeLayout(List<Photo> photos, Theme theme){
        LayoutType layout = theme.getLayoutType();

        return switch (layout) {
            case STRIP_2 -> composeStrip(photos, theme, 2, true);
            case STRIP_4 -> composeStrip(photos, theme, 4, true);
            case COLLAGE_2x2 -> composeCollage2x2(photos, theme);
            case COLLAGE_3 -> composeCollage3(photos, theme);
            default -> applyTheme(photos.get(0), theme);
        };
    }

    private static Image composeStrip(List<Photo> photos, Theme theme, int count, boolean vertical) {
        int photoWidth = 600;
        int photoHeight = 400;
        int spacing = 10;

        BufferedImage[] processed = new BufferedImage[count];
        for (int i = 0; i < count && i < photos.size(); i++) {
            Image filtered = applyFilter(photos.get(i).getOriginalImage(),
                    theme.getFilterType(),
                    theme.getFilterIntensity());
            Image resized = ImageUtils.resizeImage(filtered, photoWidth, photoHeight);
            processed[i] = SwingFXUtils.fromFXImage(resized, null);
        }

        int canvasWidth, canvasHeight;
        if (vertical) {
            canvasWidth = photoWidth;
            canvasHeight = (photoHeight * count) + (spacing * (count - 1));
        } else {
            canvasWidth = (photoWidth * count) + (spacing * (count - 1));
            canvasHeight = photoHeight;
        }

        int borderWidth = theme.getBorderWidth();
        canvasWidth += borderWidth * 2;
        canvasHeight += borderWidth * 2;

        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(toAwtColor(theme.getBackgroundColor()));
        g2d.fillRect(0, 0, canvasWidth, canvasHeight);

        for (int i = 0; i < count && i < processed.length && processed[i] != null; i++) {
            int x, y;
            if (vertical) {
                x = borderWidth;
                y = borderWidth + (i * (photoHeight + spacing));
            } else {
                x = borderWidth + (i * (photoWidth + spacing));
                y = borderWidth;
            }
            g2d.drawImage(processed[i], x, y, null);
        }

        g2d.dispose();

        Image result = SwingFXUtils.toFXImage(canvas, null);
        return addTextOverlays(result, theme);
    }

    private static Image composeCollage2x2(List<Photo> photos, Theme theme) {
        int photoSize = 400;
        int spacing = 10;
        int borderWidth = theme.getBorderWidth();

        int canvasWidth = (photoSize * 2) + spacing + (borderWidth * 2);
        int canvasHeight = (photoSize * 2) + spacing + (borderWidth * 2);

        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(toAwtColor(theme.getBackgroundColor()));
        g2d.fillRect(0, 0, canvasWidth, canvasHeight);

        int[][] positions = {
                {borderWidth, borderWidth},
                {borderWidth + photoSize + spacing, borderWidth},
                {borderWidth, borderWidth + photoSize + spacing},
                {borderWidth + photoSize + spacing, borderWidth + photoSize + spacing}
        };

        for (int i = 0; i < 4 && i < photos.size(); i++) {
            Image filtered = applyFilter(photos.get(i).getOriginalImage(),
                    theme.getFilterType(),
                    theme.getFilterIntensity());
            Image resized = ImageUtils.resizeImage(filtered, photoSize, photoSize);
            BufferedImage buffered = SwingFXUtils.fromFXImage(resized, null);

            g2d.drawImage(buffered, positions[i][0], positions[i][1], null);
        }

        g2d.dispose();

        Image result = SwingFXUtils.toFXImage(canvas, null);
        return addTextOverlays(result, theme);
    }

    private static Image composeCollage3(List<Photo> photos, Theme theme) {
        int largePhotoWidth = 600;
        int largePhotoHeight = 800;
        int smallPhotoWidth = 300;
        int smallPhotoHeight = 400;
        int spacing = 10;
        int borderWidth = theme.getBorderWidth();

        int canvasWidth = largePhotoWidth + smallPhotoWidth + spacing + (borderWidth * 2);
        int canvasHeight = largePhotoHeight + (borderWidth * 2);

        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(toAwtColor(theme.getBackgroundColor()));
        g2d.fillRect(0, 0, canvasWidth, canvasHeight);

        if (!photos.isEmpty()) {
            Image filtered = applyFilter(photos.get(0).getOriginalImage(),
                    theme.getFilterType(), theme.getFilterIntensity());
            Image resized = ImageUtils.resizeImage(filtered, largePhotoWidth, largePhotoHeight);
            BufferedImage buffered = SwingFXUtils.fromFXImage(resized, null);
            g2d.drawImage(buffered, borderWidth, borderWidth, null);
        }

        int smallX = borderWidth + largePhotoWidth + spacing;
        for (int i = 1; i < 3 && i < photos.size(); i++) {
            Image filtered = applyFilter(photos.get(i).getOriginalImage(),
                    theme.getFilterType(), theme.getFilterIntensity());
            Image resized = ImageUtils.resizeImage(filtered, smallPhotoWidth, smallPhotoHeight);
            BufferedImage buffered = SwingFXUtils.fromFXImage(resized, null);

            int smallY = borderWidth + ((i - 1) * (smallPhotoHeight + spacing));
            g2d.drawImage(buffered, smallX, smallY, null);
        }

        g2d.dispose();

        Image result = SwingFXUtils.toFXImage(canvas, null);
        return addTextOverlays(result, theme);
    }

    private static java.awt.Color toAwtColor(Color fxColor) {
        return new java.awt.Color(
                (float) fxColor.getRed(),
                (float) fxColor.getGreen(),
                (float) fxColor.getBlue(),
                (float) fxColor.getOpacity()
        );
    }

    public static Image prepareForPrint(Image image) {
        // For now, just ensure it's high quality
        // In production, you might resize to exact print dimensions
        return image;
    }
}
