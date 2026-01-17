package com.photobooth.ui;

import com.photobooth.model.Photo;
import com.photobooth.service.PhotoManager;
import com.photobooth.util.ImageUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

import static com.photobooth.util.Constants.*;

public class PhotoGalleryView extends BorderPane {

    private final PhotoManager photoManager;
    private final TilePane photoGrid;
    private final ScrollPane scrollPane;
    private final Label emptyLabel;

    private final Map<Photo, VBox> photoBoxMap;

    private PhotoSelectionListener selectionListener;

    @FunctionalInterface
    public interface PhotoSelectionListener {
        void onPhotoSelected(Photo photo);
    }

    public PhotoGalleryView(PhotoManager photoManager) {
        this.photoManager = photoManager;
        this.photoBoxMap = new HashMap<>();

        emptyLabel = new Label(MSG_NO_PHOTOS);
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");

        photoGrid = new TilePane();
        photoGrid.setPadding(new Insets(GALLERY_SPACING));
        photoGrid.setHgap(GALLERY_SPACING);
        photoGrid.setVgap(GALLERY_SPACING);
        photoGrid.setPrefColumns(GALLERY_COLUMNS);
        photoGrid.setAlignment(Pos.TOP_LEFT);

        scrollPane = new ScrollPane(photoGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + COLOR_BACKGROUND + ";");

        updateDisplay();
    }

    public void refresh() {
        photoGrid.getChildren().clear();
        photoBoxMap.clear();

        if (photoManager.isEmpty()) {
            updateDisplay();
            return;
        }

        for (Photo photo : photoManager.getAllPhotos()) {
            VBox photoBox = createPhotoBox(photo);
            photoGrid.getChildren().add(photoBox);
            photoBoxMap.put(photo, photoBox);
        }

        updateDisplay();

        if (photoManager.hasSelection()) {
            highlightSelection(photoManager.getSelectedPhoto());
        }
    }

    private VBox createPhotoBox(Photo photo) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: " + COLOR_BORDER + "; " +
                        "-fx-border-width: 2px; " +
                        "-fx-cursor: hand;"
        );

        Image thumbnail = ImageUtils.createThumbnail(photo.getCurrentImage());
        ImageView imageView = new ImageView(thumbnail);
        imageView.setFitWidth(THUMBNAIL_SIZE);
        imageView.setFitHeight(THUMBNAIL_SIZE);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(photo.getFileName());
        nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        nameLabel.setMaxWidth(THUMBNAIL_SIZE);
        nameLabel.setWrapText(false);
        nameLabel.setAlignment(Pos.CENTER);

        Label sizeLabel = new Label(photo.getFileSizeFormatted());
        sizeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");

        Label themeLabel = new Label();
        if (photo.hasThemeApplied()) {
            themeLabel.setText("✓ " + photo.getAppliedTheme().getName());
            themeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + COLOR_PRIMARY + ";");
        }

        box.getChildren().addAll(imageView, nameLabel, sizeLabel);
        if (photo.hasThemeApplied()) {
            box.getChildren().add(themeLabel);
        }

        box.setOnMouseClicked(event -> handlePhotoClick(photo, box));

        box.setOnMouseEntered(event -> {
            if (!isSelected(photo)) {
                box.setStyle(
                        "-fx-background-color: #f5f5f5; " +
                                "-fx-border-color: " + COLOR_PRIMARY + "; " +
                                "-fx-border-width: 2px; " +
                                "-fx-cursor: hand;"
                );
            }
        });

        box.setOnMouseExited(event -> {
            if (!isSelected(photo)) {
                box.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-border-color: " + COLOR_BORDER + "; " +
                                "-fx-border-width: 2px; " +
                                "-fx-cursor: hand;"
                );
            }
        });

        return box;
    }

    private void handlePhotoClick(Photo photo, VBox box) {
        photoManager.selectPhoto(photo);

        clearAllHighlights();
        highlightSelection(photo);

        if (selectionListener != null) {
            selectionListener.onPhotoSelected(photo);
        }
    }

    private void highlightSelection(Photo photo) {
        VBox box = photoBoxMap.get(photo);
        if (box != null) {
            box.setStyle(
                    "-fx-background-color: #e3f2fd; " +
                            "-fx-border-color: " + COLOR_PRIMARY + "; " +
                            "-fx-border-width: 3px; " +
                            "-fx-cursor: hand;"
            );
        }
    }

    private void clearAllHighlights() {
        for (VBox box : photoBoxMap.values()) {
            box.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: " + COLOR_BORDER + "; " +
                            "-fx-border-width: 2px; " +
                            "-fx-cursor: hand;"
            );
        }
    }

    private boolean isSelected(Photo photo) {
        return photoManager.hasSelection() &&
                photoManager.getSelectedPhoto().equals(photo);
    }

    private void updateDisplay() {
        if (photoManager.isEmpty()) {
            setCenter(createEmptyState());
        } else {
            setCenter(scrollPane);
        }
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));

        Label iconLabel = new Label("📷");
        iconLabel.setStyle("-fx-font-size: 72px;");

        Label messageLabel = new Label(MSG_NO_PHOTOS);
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");

        Label instructionLabel = new Label("Click 'Upload Photos' to get started");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");

        emptyBox.getChildren().addAll(iconLabel, messageLabel, instructionLabel);

        return emptyBox;
    }

    public void addPhoto(Photo photo) {
        if (photo != null) {
            refresh();
        }
    }

    public void removePhoto(Photo photo) {
        if (photo != null) {
            photoBoxMap.remove(photo);
            refresh();
        }
    }

    public void clear() {
        photoBoxMap.clear();
        refresh();
    }

    public void setSelectionListener(PhotoSelectionListener listener) {
        this.selectionListener = listener;
    }

    public TilePane getPhotoGrid() {
        return photoGrid;
    }

    public void updatePhoto(Photo photo) {
        if (photo == null || !photoBoxMap.containsKey(photo)) {
            return;
        }

        VBox oldBox = photoBoxMap.get(photo);
        photoGrid.getChildren().remove(oldBox);

        VBox newBox = createPhotoBox(photo);
        int index = photoManager.getAllPhotos().indexOf(photo);

        if (index >= 0 && index < photoGrid.getChildren().size()) {
            photoGrid.getChildren().add(index, newBox);
        } else {
            photoGrid.getChildren().add(newBox);
        }

        photoBoxMap.put(photo, newBox);

        if (isSelected(photo)) {
            highlightSelection(photo);
        }
    }

    public VBox getSelectedPhotoBox() {
        if (!photoManager.hasSelection()) {
            return null;
        }
        return photoBoxMap.get(photoManager.getSelectedPhoto());
    }

    public void scrollToPhoto(Photo photo) {
        VBox box = photoBoxMap.get(photo);
        if (box != null) {
            // Calculate approximate scroll position
            double vValue = photoGrid.getChildren().indexOf(box) /
                    (double) photoGrid.getChildren().size();
            scrollPane.setVvalue(vValue);
        }
    }

}
