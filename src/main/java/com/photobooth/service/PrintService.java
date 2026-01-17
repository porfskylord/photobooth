package com.photobooth.service;

import com.photobooth.model.Photo;
import javafx.embed.swing.SwingFXUtils;
import javafx.print.*;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.image.Image;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.attribute.standard.PrintQuality;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;


public class PrintService {


    public enum PrintStatus {
        SUCCESS,
        CANCELLED,
        ERROR,
        NO_PRINTER
    }


    public static class PrintResult {
        private final PrintStatus status;
        private final String message;

        public PrintResult(PrintStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public PrintStatus getStatus() { return status; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return status == PrintStatus.SUCCESS; }
    }


    public static PrintResult printPhotoJavaFX(Photo photo, String paperSize,
                                               String orientation, int copies, boolean color) {
        try {
            Printer printer = Printer.getDefaultPrinter();

            if (printer == null) {
                return new PrintResult(PrintStatus.NO_PRINTER,
                        "No printer found. Please connect a printer and try again.");
            }

            PrinterJob printerJob = PrinterJob.createPrinterJob(printer);

            if (printerJob == null) {
                return new PrintResult(PrintStatus.ERROR,
                        "Failed to create print job.");
            }

            PageLayout pageLayout = configureFXPageLayout(printer, paperSize, orientation);

            boolean proceed = printerJob.showPrintDialog(null);

            if (!proceed) {
                printerJob.endJob();
                return new PrintResult(PrintStatus.CANCELLED,
                        "Print job cancelled by user.");
            }

            Image imageToPrint = photo.getCurrentImage();

            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(imageToPrint);

            double pageWidth = pageLayout.getPrintableWidth();
            double pageHeight = pageLayout.getPrintableHeight();

            imageView.setPreserveRatio(true);
            imageView.setFitWidth(pageWidth);
            imageView.setFitHeight(pageHeight);

            for (int i = 0; i < copies; i++) {
                boolean success = printerJob.printPage(pageLayout, imageView);

                if (!success) {
                    printerJob.endJob();
                    return new PrintResult(PrintStatus.ERROR,
                            "Failed to print page " + (i + 1) + " of " + copies);
                }
            }

            boolean jobSuccess = printerJob.endJob();

            if (jobSuccess) {
                return new PrintResult(PrintStatus.SUCCESS,
                        "Successfully printed " + copies + " cop" + (copies > 1 ? "ies" : "y") +
                                " of " + photo.getFileName());
            } else {
                return new PrintResult(PrintStatus.ERROR,
                        "Print job failed to complete.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new PrintResult(PrintStatus.ERROR,
                    "Print error: " + e.getMessage());
        }
    }


    private static PageLayout configureFXPageLayout(Printer printer, String paperSize, String orientation) {
        PrinterAttributes printerAttrs = printer.getPrinterAttributes();

        Paper paper = getPaperSize(paperSize);

        PageOrientation pageOrientation;
        if (orientation.equalsIgnoreCase("Portrait")) {
            pageOrientation = PageOrientation.PORTRAIT;
        } else if (orientation.equalsIgnoreCase("Landscape")) {
            pageOrientation = PageOrientation.LANDSCAPE;
        } else {
            pageOrientation = PageOrientation.PORTRAIT; // Default
        }

        return printer.createPageLayout(
                paper,
                pageOrientation,
                Printer.MarginType.DEFAULT
        );
    }


    private static Paper getPaperSize(String paperSize) {
        if (paperSize.startsWith("4x6")) {
            return Paper.NA_LETTER;
        } else if (paperSize.startsWith("5x7")) {
            return Paper.NA_LETTER;
        } else if (paperSize.startsWith("8x10")) {
            return Paper.NA_LETTER;
        } else if (paperSize.startsWith("A4")) {
            return Paper.A4;
        } else {
            return Paper.NA_LETTER;
        }
    }


    public static PrintResult printPhotoAWT(Photo photo, String paperSize,
                                            String orientation, int copies, boolean color) {
        try {
            javax.print.PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

            if (printService == null) {
                return new PrintResult(PrintStatus.NO_PRINTER,
                        "No printer found. Please connect a printer and try again.");
            }

            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

            attributes.add(new Copies(copies));

            if (orientation.equalsIgnoreCase("Portrait")) {
                attributes.add(OrientationRequested.PORTRAIT);
            } else if (orientation.equalsIgnoreCase("Landscape")) {
                attributes.add(OrientationRequested.LANDSCAPE);
            }

            if (color) {
                attributes.add(Chromaticity.COLOR);
            } else {
                attributes.add(Chromaticity.MONOCHROME);
            }

            MediaSizeName mediaSizeName = getMediaSize(paperSize);
            if (mediaSizeName != null) {
                attributes.add(mediaSizeName);
            }

            attributes.add(PrintQuality.HIGH);

            java.awt.print.PrinterJob printerJob = java.awt.print.PrinterJob.getPrinterJob();
            printerJob.setPrintService(printService);

            PageFormat pageFormat = printerJob.defaultPage();

            if (orientation.equalsIgnoreCase("Landscape")) {
                pageFormat.setOrientation(PageFormat.LANDSCAPE);
            } else {
                pageFormat.setOrientation(PageFormat.PORTRAIT);
            }

            java.awt.print.Paper paper = new java.awt.print.Paper();
            configurePaperSize(paper, paperSize);
            pageFormat.setPaper(paper);

            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(photo.getCurrentImage(), null);

            Printable printable = new PhotoPrintable(bufferedImage, pageFormat);

            printerJob.setPrintable(printable, pageFormat);

            boolean doPrint = printerJob.printDialog(attributes);

            if (!doPrint) {
                return new PrintResult(PrintStatus.CANCELLED,
                        "Print job cancelled by user.");
            }

            printerJob.print(attributes);

            return new PrintResult(PrintStatus.SUCCESS,
                    "Successfully printed " + copies + " cop" + (copies > 1 ? "ies" : "y") +
                            " of " + photo.getFileName());

        } catch (PrinterException e) {
            e.printStackTrace();
            return new PrintResult(PrintStatus.ERROR,
                    "Printer error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new PrintResult(PrintStatus.ERROR,
                    "Print error: " + e.getMessage());
        }
    }


    private static MediaSizeName getMediaSize(String paperSize) {
        if (paperSize.startsWith("4x6")) {
            return MediaSizeName.NA_LEGAL;
        } else if (paperSize.startsWith("A4")) {
            return MediaSizeName.ISO_A4;
        } else if (paperSize.contains("Letter")) {
            return MediaSizeName.NA_LETTER;
        }
        return MediaSizeName.NA_LETTER;
    }


    private static void configurePaperSize(java.awt.print.Paper paper, String paperSize) {
        double width, height;

        if (paperSize.startsWith("4x6")) {
            width = 4 * 72;
            height = 6 * 72;
        } else if (paperSize.startsWith("5x7")) {
            width = 5 * 72;
            height = 7 * 72;
        } else if (paperSize.startsWith("8x10")) {
            width = 8 * 72;
            height = 10 * 72;
        } else if (paperSize.startsWith("A4")) {
            width = 8.27 * 72;
            height = 11.69 * 72;
        } else {
            width = 8.5 * 72;
            height = 11 * 72;
        }

        paper.setSize(width, height);

        double margin = 0.5 * 72;
        paper.setImageableArea(margin, margin,
                width - 2 * margin,
                height - 2 * margin);
    }


    private static class PhotoPrintable implements Printable {
        private final BufferedImage image;
        private final PageFormat pageFormat;

        public PhotoPrintable(BufferedImage image, PageFormat pageFormat) {
            this.image = image;
            this.pageFormat = pageFormat;
        }

        @Override
        public int print(Graphics graphics, PageFormat pf, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            double pageWidth = pf.getImageableWidth();
            double pageHeight = pf.getImageableHeight();
            double pageX = pf.getImageableX();
            double pageY = pf.getImageableY();

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            double scaleX = pageWidth / imageWidth;
            double scaleY = pageHeight / imageHeight;
            double scale = Math.min(scaleX, scaleY);

            double scaledWidth = imageWidth * scale;
            double scaledHeight = imageHeight * scale;

            double x = pageX + (pageWidth - scaledWidth) / 2;
            double y = pageY + (pageHeight - scaledHeight) / 2;

            g2d.drawImage(image,
                    (int) x, (int) y,
                    (int) scaledWidth, (int) scaledHeight,
                    null);

            return PAGE_EXISTS;
        }
    }


    public static String[] getAvailablePrinters() {
        javax.print.PrintService[] printServices =
                PrintServiceLookup.lookupPrintServices(null, null);

        String[] printerNames = new String[printServices.length];
        for (int i = 0; i < printServices.length; i++) {
            printerNames[i] = printServices[i].getName();
        }

        return printerNames;
    }

    public static boolean isPrinterAvailable() {
        javax.print.PrintService defaultService =
                PrintServiceLookup.lookupDefaultPrintService();
        return defaultService != null;
    }


    public static String getDefaultPrinterName() {
        javax.print.PrintService defaultService =
                PrintServiceLookup.lookupDefaultPrintService();

        if (defaultService != null) {
            return defaultService.getName();
        }
        return "None";
    }
}