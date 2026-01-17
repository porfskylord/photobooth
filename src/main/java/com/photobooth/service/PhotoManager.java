package com.photobooth.service;

import com.photobooth.model.Photo;
import com.photobooth.model.Theme;
import com.photobooth.util.ImageUtils;
import javafx.scene.image.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PhotoManager {

    private final List<Photo> photos;
    private Photo selectedPhoto;
    private final List<Photo> selectedPhotos;

    public PhotoManager() {
        this.photos = new ArrayList<>();
        this.selectedPhoto = null;
        this.selectedPhotos = new ArrayList<>();
    }

    public Photo addPhoto(File file) {
        if (file == null || !ImageUtils.isSupportedImageFile(file)){
            System.err.println("Invalid or unsupported image file: " + file);
            return null;
        }

        Image image = ImageUtils.loadImage(file);
        if (image == null){
            System.err.println("Failed to load image: " + file);
            return null;
        }

        Photo photo = new Photo(file, image);
        photos.add(photo);

        System.out.println("Photo added: " + photo.getFileName() + " (" + photo.getFileSizeFormatted() + ")");

        return photo;
    }

    public int addPhotos(List<File> files) {
        if (files == null || files.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (File file : files){
            if (addPhoto(file) != null) {
                successCount++;
            }
        }

        System.out.println("Added: " + successCount + " of " + files.size() + " photos");
        return successCount;
    }

    public void addPhoto(Photo photo) {
        if (photo != null && !photos.contains(photo)) {
            photos.add(photo);
        }
    }

    public boolean removePhoto(Photo photo) {
        if (photo == null) {
            return false;
        }

        if (selectedPhoto == photo) {
            selectedPhoto = null;
        }

        selectedPhotos.remove(photo);

        boolean removed = photos.remove(photo);

        if (removed) {
            System.out.println("Photo removed: " + photo.getFileName());
        }

        return removed;
    }

    public boolean removePhotoById(String id) {
        Optional<Photo> photo = findPhotoById(id);
        return photo.map(this::removePhoto).orElse(false);
    }

    public void clearAll() {
        photos.clear();
        selectedPhoto = null;
        selectedPhotos.clear();
        System.out.println("All photos cleared");
    }

    public void selectPhoto(Photo photo) {
        if (photo != null && photos.contains(photo)){
            this.selectedPhoto = photo;
            System.out.println("Photo selected: " + photo.getFileName());
        }
    }

    public void selectPhotoById(String id) {
        findPhotoById(id).ifPresent(this::selectPhoto);
    }

    public void clearSelection() {
        this.selectedPhoto = null;
        this.selectedPhotos.clear();
    }

    public void addToSelection(Photo photo) {
        if (photo != null && photos.contains(photo) && !selectedPhotos.contains(photo)) {
            selectedPhotos.add(photo);
        }
    }

    public void removeFromSelection(Photo photo) {
        selectedPhotos.remove(photo);
    }

    public void selectMultiplePhotos(List<Photo> photoList) {
        selectedPhotos.clear();
        for (Photo photo : photoList) {
            if (photos.contains(photo)) {
                selectedPhotos.add(photo);
            }
        }
    }

    public List<Photo> getAllPhotos() {
        return Collections.unmodifiableList(photos);
    }

    public Photo getSelectedPhoto() {
        return selectedPhoto;
    }

    public List<Photo> getSelectedPhotos() {
        return Collections.unmodifiableList(selectedPhotos);
    }

    public int getPhotoCount() {
        return photos.size();
    }

    public boolean hasSelection() {
        return selectedPhoto != null;
    }

    public boolean hasMultipleSelection() {
        return !selectedPhotos.isEmpty();
    }

    public int getSelectedCount() {
        return selectedPhotos.size();
    }

    public boolean isEmpty() {
        return photos.isEmpty();
    }

    public Optional<Photo> findPhotoById(String id) {
        return photos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<Photo> getPhotosWithTheme(Theme theme) {
        if (theme == null) {
            return Collections.emptyList();
        }

        return photos.stream()
                .filter(p -> p.hasThemeApplied() &&
                        p.getAppliedTheme().equals(theme))
                .collect(Collectors.toList());
    }

    public List<Photo> getPhotosWithoutTheme() {
        return photos.stream()
                .filter(p -> !p.hasThemeApplied())
                .collect(Collectors.toList());
    }

    public List<Photo> getLandscapePhotos() {
        return photos.stream()
                .filter(Photo::isLandscape)
                .collect(Collectors.toList());
    }

    public List<Photo> getPortraitPhotos() {
        return photos.stream()
                .filter(Photo::isPortrait)
                .collect(Collectors.toList());
    }

    public void applyThemeToSelected(Theme theme) {
        if (selectedPhoto != null && theme != null) {
            selectedPhoto.setAppliedTheme(theme);
            System.out.println("Theme '" + theme.getName() +
                    "' applied to " + selectedPhoto.getFileName());
        }
    }

    public void removeThemeFromSelected() {
        if (selectedPhoto != null) {
            selectedPhoto.resetToOriginal();
            System.out.println("Theme removed from " + selectedPhoto.getFileName());
        }
    }

    public void applyThemeToAll(Theme theme) {
        if (theme == null) {
            return;
        }

        for (Photo photo : photos) {
            photo.setAppliedTheme(theme);
        }

        System.out.println("Theme '" + theme.getName() +
                "' applied to all " + photos.size() + " photos");
    }

    public void removeThemeFromAll() {
        for (Photo photo : photos) {
            photo.resetToOriginal();
        }
        System.out.println("Themes removed from all photos");
    }

    public boolean hasEnoughPhotosForTheme(Theme theme) {
        if (theme == null) {
            return false;
        }

        int required = theme.getPhotoSlots();

        if (required == 1) {
            return selectedPhoto != null;
        }

        return selectedPhotos.size() >= required;
    }

    public List<Photo> getPhotosForTheme(Theme theme) {
        if (theme == null) {
            return Collections.emptyList();
        }

        int required = theme.getPhotoSlots();

        if (required == 1) {
            if (selectedPhoto != null) {
                return List.of(selectedPhoto);
            }
            return Collections.emptyList();
        }

        if (selectedPhotos.size() >= required) {
            return new ArrayList<>(selectedPhotos.subList(0, required));
        }

        return Collections.emptyList();
    }

    public long getTotalSizeBytes() {
        return photos.stream()
                .mapToLong(Photo::getFileSizeBytes)
                .sum();
    }

    public String getTotalSizeFormatted() {
        long bytes = getTotalSizeBytes();
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    public String getSummary() {
        return String.format("Photos: %d | Selected: %d | Total Size: %s",
                photos.size(),
                selectedPhoto != null ? 1 : selectedPhotos.size(),
                getTotalSizeFormatted());
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
