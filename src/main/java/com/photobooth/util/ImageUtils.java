package com.photobooth.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;

import static com.photobooth.util.Constants.*;

public class ImageUtils {

    private ImageUtils() {
        throw new AssertionError("Cannot instantiate ImageUtils class");
    }

    public static Image loadImage(File file){
        try {
            if (!file.exists() || !file.isFile()) {
                System.err.println("File does not exist: " + file.getAbsolutePath());
                return null;
            }

            String fileUrl = file.toURI().toString();
            Image image = new Image(fileUrl);
            if (image.isError()) {
                System.err.println("Error loading image: " + file.getName());
                return null;
            }
            return image;
        }
        catch (Exception e) {
            System.err.println("Failed to load image: " + e.getMessage());
            return null;
        }
    }

    public static Image loadImage(String filePath){
        return loadImage(new File(filePath));
    }

    public static Image resizeImage(Image image, int maxWidth, int maxHeight){
        double width = image.getWidth();
        double height = image.getHeight();

        double aspectRatio = width / height;

        double newWidth = width;
        double newHeight = height;

        if (width > maxWidth) {
            newWidth = maxWidth;
            newHeight = newWidth / aspectRatio;
        }

        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = newHeight * aspectRatio;
        }

        if (newWidth >= width && newHeight >= height) {
            return image;
        }

        BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
        BufferedImage resized = resizeBufferedImage(buffered, (int) newWidth, (int) newHeight);
        return SwingFXUtils.toFXImage(resized, null);
    }

    public static Image createThumbnail(Image image){
        return resizeImage(image, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
    }

    private static BufferedImage resizeBufferedImage(BufferedImage image, int width, int height){
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return resized;
    }

    public static Image applyGrayscale(Image image, double intensity){

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                double gray = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();

                double red = color.getRed() * (1 - intensity) + gray * intensity;
                double green = color.getGreen() * (1 - intensity) + gray * intensity;
                double blue = color.getBlue() * (1 - intensity) + gray * intensity;

                writer.setColor(x, y, Color.color(red, green, blue, color.getOpacity()));
            }
        }
        return result;
    }

    public static Image applySepia(Image image, double intensity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                double tr = 0.393 * color.getRed() + 0.769 * color.getGreen() + 0.189 * color.getBlue();
                double tg = 0.349 * color.getRed() + 0.686 * color.getGreen() + 0.168 * color.getBlue();
                double tb = 0.272 * color.getRed() + 0.534 * color.getGreen() + 0.131 * color.getBlue();

                tr = Math.min(1.0, tr);
                tg = Math.min(1.0, tg);
                tb = Math.min(1.0, tb);

                double r = color.getRed() * (1 - intensity) + tr * intensity;
                double g = color.getGreen() * (1 - intensity) + tg * intensity;
                double b = color.getBlue() * (1 - intensity) + tb * intensity;

                writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
            }
        }

        return result;
    }

    public static Image applyVibrant(Image image, double intensity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        double saturation = 1.0 + intensity;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                double hue = color.getHue();
                double sat = color.getSaturation();
                double bright = color.getBrightness();

                sat = Math.min(1.0, sat * saturation);

                Color newColor = Color.hsb(hue, sat, bright, color.getOpacity());
                writer.setColor(x, y, newColor);
            }
        }

        return result;
    }

    public static Image applyBrightness(Image image, double intensity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                double r = Math.max(0.0, Math.min(1.0, color.getRed() + intensity));
                double g = Math.max(0.0, Math.min(1.0, color.getGreen() + intensity));
                double b = Math.max(0.0, Math.min(1.0, color.getBlue() + intensity));

                writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
            }
        }

        return result;
    }

    public static Image applyContrast(Image image, double intensity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        double contrast = 1.0 + intensity;
        double factor = (259.0 * (contrast + 255.0)) / (255.0 * (259.0 - contrast));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                double r = factor * (color.getRed() - 0.5) + 0.5;
                double g = factor * (color.getGreen() - 0.5) + 0.5;
                double b = factor * (color.getBlue() - 0.5) + 0.5;

                r = Math.max(0.0, Math.min(1.0, r));
                g = Math.max(0.0, Math.min(1.0, g));
                b = Math.max(0.0, Math.min(1.0, b));

                writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
            }
        }

        return result;
    }

    public static Image applyWarmTone(Image image, double intensity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                double r = Math.min(1.0, color.getRed() + 0.1 * intensity);
                double g = Math.min(1.0, color.getGreen() + 0.05 * intensity);
                double b = Math.max(0.0, color.getBlue() - 0.1 * intensity);

                writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
            }
        }

        return result;
    }

    public static Image applyCoolTone(Image image, double intensity) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);

                double r = Math.max(0.0, color.getRed() - 0.1 * intensity);
                double g = color.getGreen();
                double b = Math.min(1.0, color.getBlue() + 0.1 * intensity);

                writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
            }
        }

        return result;
    }

    public static boolean saveImage(Image image, File file, String format) {
        try {
            BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
            return ImageIO.write(buffered, format, file);
        } catch (IOException e) {
            System.err.println("Failed to save image: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveAsPNG(Image image, File file) {
        return saveImage(image, file, "png");
    }

    public static boolean saveAsJPEG(Image image, File file) {
        return saveImage(image, file, "jpg");
    }

    public static boolean isSupportedImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".bmp") ||
                name.endsWith(".gif");
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1).toLowerCase() : "";
    }
}
