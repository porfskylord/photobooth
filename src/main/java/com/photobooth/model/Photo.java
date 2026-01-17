package com.photobooth.model;

import javafx.scene.image.Image;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Objects;

public class Photo {

    private final String id;
    private final File sourceFile;
    private final Image originalImage;
    private Image processedImage;;
    private final LocalDateTime uploadAt;
    private Theme appliedTheme;;
    private String fileName;

    public Photo(File sourceFile, Image originalImage){
        this.id = generateId();
        this.sourceFile = sourceFile;
        this.originalImage = originalImage;
        this.processedImage = originalImage;
        this.uploadAt = LocalDateTime.now();
        this.fileName = sourceFile.getName();
        this.appliedTheme = null;
    }

    private String generateId(){
        return "PHOTO_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    public String getId(){
        return id;
    }

    public File getSourceFile(){
        return sourceFile;
    }

    public Image getOriginalImage(){
        return originalImage;
    }

    public Image getProcessedImage(){
        return processedImage;
    }



    public LocalDateTime getUploadedAt(){
        return uploadAt;
    }

    public Theme getAppliedTheme(){
        return appliedTheme;
    }

    public String getFileName(){
        return fileName;
    }

    public String getFilePath(){
        return sourceFile.getAbsolutePath();
    }

    public Image getCurrentImage(){
        return processedImage != null ? processedImage : originalImage;
    }

    public boolean hasThemeApplied(){
        return appliedTheme != null;
    }

    public void setProcessedImage(Image processedImage) {
        this.processedImage = processedImage;
    }

    public void setAppliedTheme(Theme theme){
        this.appliedTheme = theme;
    }

    public void resetToOriginal(){
        this.processedImage = this.originalImage;
        this.appliedTheme = null;
    }

    public double getWidth(){
        return originalImage.getWidth();
    }

    public double getHeight(){
        return originalImage.getHeight();
    }

    public double getAspectRatio(){
        return getWidth() / getHeight();
    }

    public boolean isLandscape(){
        return getWidth() > getHeight();
    }

    public boolean isPortrait(){
        return getHeight() > getWidth();
    }

    public long getFileSizeBytes(){
        return sourceFile.length();
    }

    public String getFileSizeFormatted(){
        long bytes = getFileSizeBytes();
        if(bytes < 1024) return bytes + " B";
        if(bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(id, photo.id);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

    @Override
    public String toString(){
        return "Photo{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", size=" + getFileSizeFormatted() +
                ", dimensions=" + (int)getWidth() + "x" + (int)getHeight() +
                ", hasTheme=" + hasThemeApplied() +
                '}';
    }
}
