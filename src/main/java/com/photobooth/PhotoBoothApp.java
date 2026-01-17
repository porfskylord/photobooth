package com.photobooth;

import com.photobooth.model.Photo;
import com.photobooth.model.Theme;
import com.photobooth.service.PhotoManager;
import com.photobooth.service.PrintService;
import com.photobooth.ui.PhotoGalleryView;
import com.photobooth.ui.PrintPreviewDialog;
import com.photobooth.ui.ThemeSelectorPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

import static com.photobooth.util.Constants.*;


public class PhotoBoothApp extends Application {

    private PhotoManager photoManager;

    private PhotoGalleryView galleryView;
    private ThemeSelectorPanel themeSelectorPanel;

    private Button uploadButton;
    private Button printButton;
    private Button clearButton;
    private Label statusLabel;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        initializeServices();

        BorderPane root = createMainLayout();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle(APP_TITLE + " - v" + APP_VERSION);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateUIState();
    }

    private void initializeServices() {
        photoManager = new PhotoManager();
        System.out.println("Photo Booth initialized");
        System.out.println("Printer available: " + PrintService.isPrinterAvailable());
        if (PrintService.isPrinterAvailable()) {
            System.out.println("Default printer: " + PrintService.getDefaultPrinterName());
        }
    }


    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(PANEL_PADDING));

        HBox toolbar = createToolbar();
        root.setTop(toolbar);

        galleryView = new PhotoGalleryView(photoManager);
        galleryView.setSelectionListener(this::onPhotoSelected);
        root.setCenter(galleryView);

        themeSelectorPanel = new ThemeSelectorPanel(photoManager);
        themeSelectorPanel.setThemeAppliedListener(this::onThemeApplied);
        root.setRight(themeSelectorPanel);

        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        return root;
    }


    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: " + COLOR_PANEL + ";");

        uploadButton = new Button("📁 Upload Photos");
        uploadButton.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 20px;"
        );
        uploadButton.setOnAction(e -> handleUpload());

        printButton = new Button("🖨 Print");
        printButton.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px;"
        );
        printButton.setDisable(true);
        printButton.setOnAction(e -> handlePrint());

        clearButton = new Button("🗑 Clear All");
        clearButton.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 20px;"
        );
        clearButton.setDisable(true);
        clearButton.setOnAction(e -> handleClear());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label printerLabel = new Label();
        if (PrintService.isPrinterAvailable()) {
            printerLabel.setText("🖨 Printer: " + PrintService.getDefaultPrinterName());
            printerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        } else {
            printerLabel.setText("⚠ No printer detected");
            printerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d32f2f;");
        }

        toolbar.getChildren().addAll(
                uploadButton,
                printButton,
                clearButton,
                spacer,
                printerLabel
        );

        return toolbar;
    }


    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: " + COLOR_PANEL + ";");

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px;");

        statusBar.getChildren().add(statusLabel);

        return statusBar;
    }

    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photos");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                FILE_CHOOSER_DESCRIPTION,
                SUPPORTED_IMAGE_EXTENSIONS
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            int count = photoManager.addPhotos(selectedFiles);

            galleryView.refresh();

            statusLabel.setText("Loaded " + count + " photo(s)");

            if (count > 0) {
                showInfo("Photos Loaded",
                        "Successfully loaded " + count + " photo(s).");
            }

            updateUIState();
        }
    }


    private void handlePrint() {
        if (!photoManager.hasSelection()) {
            showWarning("No Photo Selected", "Please select a photo to print.");
            return;
        }

        if (!PrintService.isPrinterAvailable()) {
            showWarning("No Printer",
                    "No printer detected. Please connect a printer and try again.");
            return;
        }

        Photo selectedPhoto = photoManager.getSelectedPhoto();

        PrintPreviewDialog previewDialog = new PrintPreviewDialog(selectedPhoto, primaryStage);

        if (previewDialog.showAndWaitForConfirmation()) {
            String paperSize = previewDialog.getPaperSize();
            String orientation = previewDialog.getOrientation();
            int copies = previewDialog.getCopies();
            boolean color = previewDialog.isColorPrint();

            statusLabel.setText("Printing...");

            PrintService.PrintResult result = PrintService.printPhotoJavaFX(
                    selectedPhoto,
                    paperSize,
                    orientation,
                    copies,
                    color
            );

            if (result.isSuccess()) {
                statusLabel.setText("Print complete!");
                showInfo("Print Success", result.getMessage());
            } else if (result.getStatus() == PrintService.PrintStatus.CANCELLED) {
                statusLabel.setText("Print cancelled");
            } else {
                statusLabel.setText("Print failed");
                showError("Print Error", result.getMessage());
            }
        } else {
            statusLabel.setText("Print cancelled");
        }
    }


    private void handleClear() {
        if (photoManager.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Photos");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will remove all loaded photos. This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                photoManager.clearAll();
                galleryView.refresh();
                statusLabel.setText("All photos cleared");
                updateUIState();
            }
        });
    }


    private void onPhotoSelected(Photo photo) {
        statusLabel.setText("Selected: " + photo.getFileName());
        updateUIState();

        themeSelectorPanel.updateApplyButtonState();
    }


    private void onThemeApplied(Theme theme, Photo photo) {
        galleryView.updatePhoto(photo);

        statusLabel.setText("Theme '" + theme.getName() + "' applied to " + photo.getFileName());

        updateUIState();
    }


    private void updateUIState() {
        boolean hasPhotos = !photoManager.isEmpty();
        boolean hasSelection = photoManager.hasSelection();

        clearButton.setDisable(!hasPhotos);
        printButton.setDisable(!hasSelection);

        themeSelectorPanel.updateApplyButtonState();

        if (!hasPhotos) {
            statusLabel.setText("No photos loaded. Click 'Upload Photos' to get started.");
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}