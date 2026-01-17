package com.photobooth.model;

import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public class Theme {

    private final String id;
    private String name;

    private LayoutType layoutType;
    private int photoSlots;

    private BorderStyle borderStyle;
    private Color borderColor;
    private int borderWidth;
    private int cornerRadius;
    private boolean hasShadow;

    private Color backgroundColor;
    private BackgroundPattern backgroundPattern;

    private FilterType filterType;
    private double filterIntensity;

    private TextOverlay headerText;
    private TextOverlay footerText;

    private String logoPath;
    private LogoPosition logoPosition;
    private boolean qrCodeEnabled;

    private boolean isCustom;


    public enum LayoutType {
        SINGLE("Single Photo", 1),
        STRIP_2("2-Photo Strip", 2),
        STRIP_4("4-Photo Strip", 4),
        COLLAGE_2x2("2x2 Collage", 4),
        COLLAGE_3("3-Photo Collage", 3);

        private final String displayName;
        private final int slots;

        LayoutType(String displayName, int slots) {
            this.displayName = displayName;
            this.slots = slots;
        }

        public String getDisplayName(){return displayName;}
        public int getSlots(){return slots;}
    }

    public enum BorderStyle {
        NONE("No Border"),
        SOLID("Solid Border"),
        ROUNDED("Rounded Corners"),
        POLAROID("Polaroid Style"),
        SHADOW("With Shadow");

        private final String displayName;

        BorderStyle(String displayName){
            this.displayName = displayName;
        }

        public String getDisplayName(){return displayName;}
    }

    public enum BackgroundPattern {
        SOLID("Solid Color"),
        GRADIENT("Gradient"),
        DOTS("Polka Dots"),
        STRIPES("Stripes"),
        NONE("Transparent");

        private final String displayName;

        BackgroundPattern(String displayName){
            this.displayName = displayName;
        }

        public String getDisplayName(){return displayName;}

    }

    public enum FilterType {
        NONE("No Filter"),
        GRAYSCALE("Black & White"),
        SEPIA("Vintage Sepia"),
        VIBRANT("Vibrant Colors"),
        WARM("Warm Tone"),
        COOL("Cool Tone"),
        BRIGHTNESS("Bright"),
        CONTRAST("High Contrast");

        private final String displayName;

        FilterType(String displayName){
            this.displayName = displayName;
        }

        public String getDisplayName(){return displayName;}

    }

    public enum LogoPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER;
    }

    public static class TextOverlay {
        private String text;
        private String fontFamily;
        private int fontSize;
        private FontWeight fontWeight;
        private Color textColor;
        private Pos position;
        private boolean enabled;

        public TextOverlay(String text, String fontFamily, int fontSize,
                           FontWeight fontWeight, Color textColor, Pos position){
            this.text = text;
            this.fontFamily = fontFamily;
            this.fontSize = fontSize;
            this.fontWeight = fontWeight;
            this.textColor = textColor;
            this.position = position;
            this.enabled = true;

        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }

        public int getFontSize() { return fontSize; }
        public void setFontSize(int fontSize) { this.fontSize = fontSize; }

        public FontWeight getFontWeight() { return fontWeight; }
        public void setFontWeight(FontWeight fontWeight) { this.fontWeight = fontWeight; }

        public Color getTextColor() { return textColor; }
        public void setTextColor(Color textColor) { this.textColor = textColor; }

        public Pos getPosition() { return position; }
        public void setPosition(Pos position) { this.position = position; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public Font getFont(){ return Font.font(fontFamily, fontWeight, fontSize); }
    }

    public Theme(String name, LayoutType layoutType, BorderStyle borderStyle,
                 Color borderColor, int borderWidth, Color backgroundColor){
        this.id = generateId();
        this.name = name;
        this.layoutType = layoutType;
        this.photoSlots = layoutType.getSlots();
        this.borderStyle = borderStyle;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.cornerRadius = 0;
        this.hasShadow = false;
        this.backgroundColor = backgroundColor;
        this.backgroundPattern = BackgroundPattern.SOLID;
        this.filterType = FilterType.NONE;
        this.filterIntensity = 0.0;
        this.isCustom = false;
        this.headerText = new TextOverlay("", "Arial", 24, FontWeight.BOLD, Color.BLACK, Pos.CENTER);
        this.headerText.setEnabled(false);
        this.footerText = new TextOverlay("", "Arial", 18, FontWeight.NORMAL, Color.GRAY, Pos.BOTTOM_CENTER);
        this.footerText.setEnabled(false);
        this.logoPath = null;
        this.logoPosition = LogoPosition.BOTTOM_RIGHT;
        this.qrCodeEnabled = false;

    }

    public Theme(String name, LayoutType layoutType){
        this(name, layoutType, BorderStyle.SOLID, Color.WHITE, 10, Color.WHITE);
    }

    private String generateId(){
        return "THEME_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LayoutType getLayoutType() { return layoutType; }
    public int getPhotoSlots() { return photoSlots; }
    public BorderStyle getBorderStyle() { return borderStyle; }
    public Color getBorderColor() { return borderColor; }
    public int getBorderWidth() { return borderWidth; }
    public int getCornerRadius() { return cornerRadius; }
    public boolean hasShadow() { return hasShadow; }
    public Color getBackgroundColor() { return backgroundColor; }
    public BackgroundPattern getBackgroundPattern() { return backgroundPattern; }
    public FilterType getFilterType() { return filterType; }
    public double getFilterIntensity() { return filterIntensity; }
    public TextOverlay getHeaderText() { return headerText; }
    public TextOverlay getFooterText() { return footerText; }
    public String getLogoPath() { return logoPath; }
    public LogoPosition getLogoPosition() { return logoPosition; }
    public boolean isQrCodeEnabled() { return qrCodeEnabled; }
    public boolean isCustom() { return isCustom; }

    public void setName(String name) { this.name = name; }
    public void setLayoutType(LayoutType layoutType) {
        this.layoutType = layoutType;
        this.photoSlots = layoutType.getSlots();
    }
    public void setBorderStyle(BorderStyle borderStyle) { this.borderStyle = borderStyle; }
    public void setBorderColor(Color borderColor) { this.borderColor = borderColor; }
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = Math.max(0, borderWidth);
    }
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = Math.max(0, cornerRadius);
    }
    public void setShadow(boolean hasShadow) { this.hasShadow = hasShadow; }
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public void setBackgroundPattern(BackgroundPattern backgroundPattern) {
        this.backgroundPattern = backgroundPattern;
    }
    public void setFilterType(FilterType filterType) { this.filterType = filterType; }
    public void setFilterIntensity(double intensity) {
        this.filterIntensity = Math.max(0.0, Math.min(1.0, intensity));
    }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
    public void setLogoPosition(LogoPosition logoPosition) {
        this.logoPosition = logoPosition;
    }
    public void setQrCodeEnabled(boolean qrCodeEnabled) {
        this.qrCodeEnabled = qrCodeEnabled;
    }
    public void setCustom(boolean custom) { this.isCustom = custom; }

    public boolean hasBorder() {
        return borderStyle != BorderStyle.NONE && borderWidth > 0;
    }

    public boolean hasFilter() {
        return filterType != FilterType.NONE && filterIntensity > 0;
    }

    public boolean hasHeaderText() {
        return headerText.isEnabled() && !headerText.getText().isEmpty();
    }

    public boolean hasFooterText() {
        return footerText.isEnabled() && !footerText.getText().isEmpty();
    }

    public boolean hasLogo() {
        return logoPath != null && !logoPath.isEmpty();
    }

    public boolean isMultiPhoto() {
        return photoSlots > 1;
    }

    public String getBorderColorHex() {
        return colorToHex(borderColor);
    }

    public String getBackgroundColorHex() {
        return colorToHex(backgroundColor);
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public Theme copy() {
        Theme copy = new Theme(this.name + " (Copy)", this.layoutType,
                this.borderStyle, this.borderColor,
                this.borderWidth, this.backgroundColor);
        copy.setCornerRadius(this.cornerRadius);
        copy.setShadow(this.hasShadow);
        copy.setBackgroundPattern(this.backgroundPattern);
        copy.setFilterType(this.filterType);
        copy.setFilterIntensity(this.filterIntensity);
        copy.setCustom(true);

        return copy;
    }

    public static Theme createClassic() {

        return new Theme("Classic", LayoutType.SINGLE, BorderStyle.SOLID,
                Color.WHITE, 15, Color.WHITE);
    }

    public static Theme createVintage() {
        Theme theme = new Theme("Vintage", LayoutType.SINGLE, BorderStyle.POLAROID,
                Color.rgb(212, 165, 116), 20, Color.rgb(245, 245, 220));
        theme.setFilterType(FilterType.SEPIA);
        theme.setFilterIntensity(0.7);
        return theme;
    }

    public static Theme createBlackAndWhite() {
        Theme theme = new Theme("Black & White", LayoutType.SINGLE, BorderStyle.SOLID,
                Color.rgb(51, 51, 51), 12, Color.rgb(224, 224, 224));
        theme.setFilterType(FilterType.GRAYSCALE);
        theme.setFilterIntensity(1.0);
        return theme;
    }

    public static Theme createModern() {
        Theme theme = new Theme("Modern", LayoutType.SINGLE, BorderStyle.ROUNDED,
                Color.BLACK, 3, Color.WHITE);
        theme.setCornerRadius(15);
        theme.setShadow(true);
        theme.setFilterType(FilterType.CONTRAST);
        theme.setFilterIntensity(0.3);
        return theme;
    }

    public static Theme createPhotoStrip() {
        Theme theme = new Theme("Photo Strip", LayoutType.STRIP_4, BorderStyle.SOLID,
                Color.WHITE, 10, Color.WHITE);
        theme.getFooterText().setText("Photo Booth 2026");
        theme.getFooterText().setEnabled(true);
        return theme;
    }

    public static Theme createCollage() {
        Theme theme = new Theme("Collage", LayoutType.COLLAGE_2x2, BorderStyle.ROUNDED,
                Color.rgb(33, 150, 243), 8, Color.WHITE);
        theme.setCornerRadius(10);
        theme.getHeaderText().setText("Memories");
        theme.getHeaderText().setTextColor(Color.rgb(33, 150, 243));
        theme.getHeaderText().setEnabled(true);
        return theme;
    }

    public static Theme createVibrant() {
        Theme theme = new Theme("Vibrant", LayoutType.SINGLE, BorderStyle.SOLID,
                Color.rgb(255, 193, 7), 12, Color.WHITE);
        theme.setFilterType(FilterType.VIBRANT);
        theme.setFilterIntensity(0.8);
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Theme{" +
                "name='" + name + '\'' +
                ", layout=" + layoutType.getDisplayName() +
                ", slots=" + photoSlots +
                ", border=" + borderStyle.getDisplayName() +
                ", filter=" + filterType.getDisplayName() +
                ", hasText=" + (hasHeaderText() || hasFooterText()) +
                '}';
    }

}
