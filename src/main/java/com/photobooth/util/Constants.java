package com.photobooth.util;

public class Constants {

    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }

    public static final String APP_TITLE = "Photo Booth";
    public static final String APP_VERSION = "1.0";

    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;

    public static final int THUMBNAIL_SIZE = 200;
    public static final int PREVIEW_MAX_WIDTH = 800;
    public static final int PREVIEW_MAX_HEIGHT = 600;
    public static final int PRINT_DPI = 300;

    public static final String FILE_CHOOSER_DESCRIPTION = "Image Files";
    public static final String[] SUPPORTED_IMAGE_EXTENSIONS = {"*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif"};


    public static final int GALLERY_COLUMNS = 4;
    public static final int GALLERY_SPACING = 10;
    public static final int PANEL_PADDING = 10;

    public static final int THEME_PANEL_WIDTH = 250;
    public static final int TOOLBAR_HEIGHT = 60;

    public static final int DEFAULT_BORDER_WIDTH = 8;
    public static final int MAX_THEMES = 20;

    public static final String DEFAULT_PAPER_SIZE = "4x6";
    public static final int PRINT_MARGIN = 20;

    public static final String COLOR_PRIMARY = "#2196F3";
    public static final String COLOR_SECONDARY = "#FFC107";
    public static final String COLOR_BACKGROUND = "#F5F5F5";
    public static final String COLOR_PANEL = "#E0E0E0";
    public static final String COLOR_BORDER = "#CCCCCC";

    public static final String MSG_NO_PHOTOS = "No photos loaded. Click 'Upload Photos' to get started.";
    public static final String MSG_SELECT_PHOTO = "Select a photo to apply theme and print.";
    public static final String MSG_PRINT_SUCCESS = "Photo printed successfully!";
    public static final String MSG_PRINT_ERROR = "Failed to print photo. Check printer connection.";
    public static final String MSG_UPLOAD_ERROR = "Failed to load image file.";

    public static final String OUTPUT_DIRECTORY = "output";
    public static final String THEME_DIRECTORY = "themes";

    public static final String DEFAULT_FONT_FAMILY = "Arial";
    public static final int DEFAULT_HEADER_FONT_SIZE = 24;
    public static final int DEFAULT_FOOTER_FONT_SIZE = 18;



}
