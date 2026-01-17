module com.photobooth {
    requires javafx.controls;
    requires javafx.swing;
    requires java.desktop;

    exports com.photobooth;
    exports com.photobooth.model;
    exports com.photobooth.service;
    exports com.photobooth.ui;
    exports com.photobooth.util;
}