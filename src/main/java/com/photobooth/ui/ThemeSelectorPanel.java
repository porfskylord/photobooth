package com.photobooth.ui;

import com.photobooth.model.Photo;
import com.photobooth.model.Theme;
import com.photobooth.service.ImageProcessor;
import com.photobooth.service.PhotoManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.photobooth.util.Constants.*;

public class ThemeSelectorPanel extends VBox {

    private final PhotoManager photoManager;
    private final List<Theme> availableThemes;
    private Theme selectedTheme;

    private final VBox themesContainer;
    private final Map<Theme, VBox> themeBoxMap;
    private final Button applyButton;
    private final Label statusLabel;

    private ThemeAppliedListener themeAppliedListener;

    @FunctionalInterface
    public interface ThemeAppliedListener {
        void onThemeApplied(Theme theme, Photo photo);
    }

    public ThemeSelectorPanel(PhotoManager photoManager) {
        this.photoManager = photoManager;
        this.availableThemes = new ArrayList<>();
        this.themeBoxMap = new HashMap<>();

        this.setPrefWidth(THEME_PANEL_WIDTH);
        this.setPadding(new Insets(PANEL_PADDING));
        this.setSpacing(10);
        this.setStyle("-fx-background-color: " + COLOR_PANEL + ";");

        Label titleLabel = new Label("Themes");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        themesContainer = new VBox(10);
        themesContainer.setPadding(new Insets(5));

        ScrollPane scrollPane = new ScrollPane(themesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        applyButton = new Button("Apply Theme");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px;"
        );
        applyButton.setDisable(true);
        applyButton.setOnAction(e -> applySelectedTheme());

        statusLabel = new Label(MSG_SELECT_PHOTO);
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setAlignment(Pos.CENTER);

        this.getChildren().addAll(
                titleLabel,
                new Separator(),
                scrollPane,
                new Separator(),
                applyButton,
                statusLabel
        );

        loadDefaultThemes();
    }


    private void loadDefaultThemes() {
        availableThemes.clear();

        availableThemes.add(Theme.createClassic());
        availableThemes.add(Theme.createVintage());
        availableThemes.add(Theme.createBlackAndWhite());
        availableThemes.add(Theme.createModern());
        availableThemes.add(Theme.createPhotoStrip());
        availableThemes.add(Theme.createCollage());
        availableThemes.add(Theme.createVibrant());

        refreshThemesList();
    }


    public void refreshThemesList() {
        themesContainer.getChildren().clear();
        themeBoxMap.clear();

        for (Theme theme : availableThemes) {
            VBox themeBox = createThemeBox(theme);
            themesContainer.getChildren().add(themeBox);
            themeBoxMap.put(theme, themeBox);
        }
    }


    private VBox createThemeBox(Theme theme) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: " + COLOR_BORDER + "; " +
                        "-fx-border-width: 2px; " +
                        "-fx-cursor: hand;"
        );

        Label nameLabel = new Label(theme.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox detailsBox = new VBox(3);

        Label layoutLabel = new Label("Layout: " + theme.getLayoutType().getDisplayName());
        layoutLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        if (theme.hasBorder()) {
            Label borderLabel = new Label("Border: " + theme.getBorderStyle().getDisplayName());
            borderLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            detailsBox.getChildren().add(borderLabel);
        }

        if (theme.hasFilter()) {
            Label filterLabel = new Label("Filter: " + theme.getFilterType().getDisplayName());
            filterLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            detailsBox.getChildren().add(filterLabel);
        }

        if (theme.hasHeaderText() || theme.hasFooterText()) {
            Label textLabel = new Label("✓ Text overlays");
            textLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + COLOR_SECONDARY + ";");
            detailsBox.getChildren().add(textLabel);
        }

        HBox colorBox = new HBox(5);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        Label colorLabel = new Label("Colors:");
        colorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        Region borderColorSwatch = createColorSwatch(theme.getBorderColor());
        Region bgColorSwatch = createColorSwatch(theme.getBackgroundColor());

        colorBox.getChildren().addAll(colorLabel, borderColorSwatch, bgColorSwatch);

        detailsBox.getChildren().addAll(layoutLabel, colorBox);

        if (theme.isMultiPhoto()) {
            Label slotsLabel = new Label("Requires " + theme.getPhotoSlots() + " photos");
            slotsLabel.setStyle(
                    "-fx-font-size: 11px; " +
                            "-fx-text-fill: " + COLOR_PRIMARY + "; " +
                            "-fx-font-weight: bold;"
            );
            detailsBox.getChildren().add(slotsLabel);
        }

        box.getChildren().addAll(nameLabel, detailsBox);

        box.setOnMouseClicked(event -> handleThemeClick(theme, box));

        box.setOnMouseEntered(event -> {
            if (!isSelected(theme)) {
                box.setStyle(
                        "-fx-background-color: #f5f5f5; " +
                                "-fx-border-color: " + COLOR_PRIMARY + "; " +
                                "-fx-border-width: 2px; " +
                                "-fx-cursor: hand;"
                );
            }
        });

        box.setOnMouseExited(event -> {
            if (!isSelected(theme)) {
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


    private Region createColorSwatch(Color color) {
        Region swatch = new Region();
        swatch.setPrefSize(20, 20);
        swatch.setStyle(
                "-fx-background-color: " + toHex(color) + "; " +
                        "-fx-border-color: #ccc; " +
                        "-fx-border-width: 1px;"
        );
        return swatch;
    }


    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }


    private void handleThemeClick(Theme theme, VBox box) {
        selectedTheme = theme;

        clearAllHighlights();
        highlightSelection(theme);

        updateApplyButtonState();
    }


    private void highlightSelection(Theme theme) {
        VBox box = themeBoxMap.get(theme);
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
        for (VBox box : themeBoxMap.values()) {
            box.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: " + COLOR_BORDER + "; " +
                            "-fx-border-width: 2px; " +
                            "-fx-cursor: hand;"
            );
        }
    }


    private boolean isSelected(Theme theme) {
        return selectedTheme != null && selectedTheme.equals(theme);
    }


    public void updateApplyButtonState() {
        boolean canApply = false;
        String message = MSG_SELECT_PHOTO;

        if (selectedTheme == null) {
            message = "Select a theme first";
        } else if (!photoManager.hasSelection()) {
            message = "Select a photo to apply theme";
        } else if (selectedTheme.isMultiPhoto()) {
            if (!photoManager.hasEnoughPhotosForTheme(selectedTheme)) {
                message = "Select " + selectedTheme.getPhotoSlots() +
                        " photos for this layout";
            } else {
                canApply = true;
                message = "Ready to apply '" + selectedTheme.getName() + "'";
            }
        } else {
            canApply = true;
            message = "Ready to apply '" + selectedTheme.getName() + "'";
        }

        applyButton.setDisable(!canApply);
        statusLabel.setText(message);
    }


    private void applySelectedTheme() {
        if (selectedTheme == null || !photoManager.hasSelection()) {
            return;
        }

        if (!photoManager.hasEnoughPhotosForTheme(selectedTheme)) {
            showAlert("Not Enough Photos",
                    "This theme requires " + selectedTheme.getPhotoSlots() + " photos.\n" +
                            "Please select more photos.");
            return;
        }

        try {
            if (selectedTheme.isMultiPhoto()) {
                List<Photo> photos = photoManager.getPhotosForTheme(selectedTheme);
                Image processedImage = ImageProcessor.applyThemeWithLayout(photos, selectedTheme);

                if (processedImage != null) {
                    Photo targetPhoto = photos.get(0);
                    targetPhoto.setProcessedImage(processedImage);
                    targetPhoto.setAppliedTheme(selectedTheme);

                    showSuccess("Theme Applied!",
                            "'" + selectedTheme.getName() + "' applied successfully!");

                    if (themeAppliedListener != null) {
                        themeAppliedListener.onThemeApplied(selectedTheme, targetPhoto);
                    }
                }
            } else {
                Photo photo = photoManager.getSelectedPhoto();
                Image processedImage = ImageProcessor.applyTheme(photo, selectedTheme);

                if (processedImage != null) {
                    photo.setProcessedImage(processedImage);
                    photo.setAppliedTheme(selectedTheme);

                    showSuccess("Theme Applied!",
                            "'" + selectedTheme.getName() + "' applied to " +
                                    photo.getFileName());

                    if (themeAppliedListener != null) {
                        themeAppliedListener.onThemeApplied(selectedTheme, photo);
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to apply theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void addTheme(Theme theme) {
        if (theme != null && !availableThemes.contains(theme)) {
            availableThemes.add(theme);
            refreshThemesList();
        }
    }


    public void removeTheme(Theme theme) {
        if (theme != null && theme.isCustom()) {
            availableThemes.remove(theme);
            if (selectedTheme == theme) {
                selectedTheme = null;
            }
            refreshThemesList();
            updateApplyButtonState();
        }
    }

    public List<Theme> getAvailableThemes() {
        return new ArrayList<>(availableThemes);
    }


    public Theme getSelectedTheme() {
        return selectedTheme;
    }


    public void setThemeAppliedListener(ThemeAppliedListener listener) {
        this.themeAppliedListener = listener;
    }


    public void clearSelection() {
        selectedTheme = null;
        clearAllHighlights();
        updateApplyButtonState();
    }
}
