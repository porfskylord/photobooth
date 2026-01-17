package com.photobooth.ui;

import com.photobooth.model.Photo;
import com.photobooth.util.ImageUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static com.photobooth.util.Constants.*;

public class PrintPreviewDialog extends Stage {

    private final Photo photo;
    private boolean printConfirmed;

    private ImageView previewImageView;
    private ComboBox<String> paperSizeComboBox;
    private ComboBox<String> orientationComboBox;
    private Spinner<Integer> copiesSpinner;
    private CheckBox colorCheckBox;
    private Label photoInfoLabel;

    public PrintPreviewDialog(Photo photo, Stage ownerStage) {
        this.photo = photo;
        this.printConfirmed = false;

        this.initModality(Modality.APPLICATION_MODAL);
        this.initOwner(ownerStage);
        this.setTitle("Print Preview - " + photo.getFileName());
        this.setResizable(false);

        BorderPane root = createDialogContent();
        Scene scene = new Scene(root, 1200, 800);
        this.setScene(scene);
    }

    private BorderPane createDialogContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        VBox topBox = createTopSection();
        root.setTop(topBox);

        VBox centerBox = createPreviewSection();
        root.setCenter(centerBox);

        VBox rightBox = createOptionsSection();
        root.setRight(rightBox);

        HBox bottomBox = createButtonSection();
        root.setBottom(bottomBox);

        return root;
    }

    private VBox createTopSection() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label("Print Preview");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        photoInfoLabel = new Label(getPhotoInfoText());
        photoInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        box.getChildren().addAll(titleLabel, photoInfoLabel);

        return box;
    }

    private VBox createPreviewSection() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1px;");

        Label previewLabel = new Label("Preview");
        previewLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Image previewImage = photo.getCurrentImage();
        previewImageView = new ImageView(previewImage);
        previewImageView.setPreserveRatio(true);
        previewImageView.setFitWidth(PREVIEW_MAX_WIDTH);
        previewImageView.setFitHeight(PREVIEW_MAX_HEIGHT);

        StackPane imageContainer = new StackPane(previewImageView);
        imageContainer.setStyle("-fx-background-color: white; -fx-padding: 20;");
        imageContainer.setMaxWidth(PREVIEW_MAX_WIDTH + 40);
        imageContainer.setMaxHeight(PREVIEW_MAX_HEIGHT + 40);

        box.getChildren().addAll(previewLabel, imageContainer);

        return box;
    }

    private VBox createOptionsSection() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(10, 0, 10, 15));
        box.setPrefWidth(200);

        Label optionsLabel = new Label("Print Options");
        optionsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox paperSizeBox = new VBox(5);
        Label paperSizeLabel = new Label("Paper Size:");
        paperSizeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        paperSizeComboBox = new ComboBox<>();
        paperSizeComboBox.getItems().addAll(
                "4x6 (10x15 cm)",
                "5x7 (13x18 cm)",
                "8x10 (20x25 cm)",
                "A4 (21x29.7 cm)",
                "Letter (8.5x11 in)"
        );
        paperSizeComboBox.setValue("4x6 (10x15 cm)");
        paperSizeComboBox.setMaxWidth(Double.MAX_VALUE);

        paperSizeBox.getChildren().addAll(paperSizeLabel, paperSizeComboBox);

        VBox orientationBox = new VBox(5);
        Label orientationLabel = new Label("Orientation:");
        orientationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        orientationComboBox = new ComboBox<>();
        orientationComboBox.getItems().addAll("Portrait", "Landscape", "Auto");

        if (photo.isLandscape()) {
            orientationComboBox.setValue("Landscape");
        } else if (photo.isPortrait()) {
            orientationComboBox.setValue("Portrait");
        } else {
            orientationComboBox.setValue("Auto");
        }
        orientationComboBox.setMaxWidth(Double.MAX_VALUE);

        orientationBox.getChildren().addAll(orientationLabel, orientationComboBox);

        VBox copiesBox = new VBox(5);
        Label copiesLabel = new Label("Copies:");
        copiesLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        copiesSpinner = new Spinner<>(1, 10, 1);
        copiesSpinner.setEditable(true);
        copiesSpinner.setMaxWidth(Double.MAX_VALUE);

        copiesBox.getChildren().addAll(copiesLabel, copiesSpinner);

        colorCheckBox = new CheckBox("Color Print");
        colorCheckBox.setSelected(true);
        colorCheckBox.setStyle("-fx-font-size: 12px;");

        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #b3d9e6; -fx-border-width: 1px;");

        Label infoTitleLabel = new Label("ℹ Print Info");
        infoTitleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label dimensionsLabel = new Label(String.format("Image: %dx%d px",
                (int)photo.getWidth(), (int)photo.getHeight()));
        dimensionsLabel.setStyle("-fx-font-size: 11px;");

        Label sizeLabel = new Label("Size: " + photo.getFileSizeFormatted());
        sizeLabel.setStyle("-fx-font-size: 11px;");

        Label themeLabel = new Label();
        if (photo.hasThemeApplied()) {
            themeLabel.setText("Theme: " + photo.getAppliedTheme().getName());
        } else {
            themeLabel.setText("Theme: None");
        }
        themeLabel.setStyle("-fx-font-size: 11px;");

        infoBox.getChildren().addAll(infoTitleLabel, dimensionsLabel, sizeLabel, themeLabel);

        box.getChildren().addAll(
                optionsLabel,
                new Separator(),
                paperSizeBox,
                orientationBox,
                copiesBox,
                colorCheckBox,
                new Separator(),
                infoBox
        );

        return box;
    }

    private HBox createButtonSection() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(15, 0, 0, 0));
        box.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-font-size: 13px;");
        cancelButton.setOnAction(e -> handleCancel());

        Button printButton = new Button("Print");
        printButton.setPrefWidth(100);
        printButton.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold;"
        );
        printButton.setOnAction(e -> handlePrint());
        printButton.setDefaultButton(true);

        box.getChildren().addAll(cancelButton, printButton);

        return box;
    }

    private String getPhotoInfoText() {
        StringBuilder info = new StringBuilder();
        info.append(photo.getFileName());
        info.append(" • ");
        info.append(String.format("%dx%d", (int)photo.getWidth(), (int)photo.getHeight()));
        info.append(" • ");
        info.append(photo.getFileSizeFormatted());

        if (photo.hasThemeApplied()) {
            info.append(" • Theme: ").append(photo.getAppliedTheme().getName());
        }

        return info.toString();
    }

    private void handleCancel() {
        printConfirmed = false;
        this.close();
    }

    private void handlePrint() {
        if (copiesSpinner.getValue() < 1) {
            showAlert("Invalid Copies", "Number of copies must be at least 1.");
            return;
        }

        printConfirmed = true;
        this.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(this);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isPrintConfirmed() {
        return printConfirmed;
    }

    public String getPaperSize() {
        return paperSizeComboBox.getValue();
    }

    public String getOrientation() {
        return orientationComboBox.getValue();
    }

    public int getCopies() {
        return copiesSpinner.getValue();
    }

    public boolean isColorPrint() {
        return colorCheckBox.isSelected();
    }

    public Photo getPhoto() {
        return photo;
    }

    public boolean showAndWaitForConfirmation() {
        this.showAndWait();
        return printConfirmed;
    }

    public String getPrintSettingsSummary() {
        return String.format(
                "Paper: %s | Orientation: %s | Copies: %d | Color: %s",
                getPaperSize(),
                getOrientation(),
                getCopies(),
                isColorPrint() ? "Yes" : "No"
        );
    }
}