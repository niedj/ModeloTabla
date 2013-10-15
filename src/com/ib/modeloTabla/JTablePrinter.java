package com.ib.modeloTabla;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import javax.swing.JTable;

/**
 * Convenience method to print the information contained in a jTable The reason
 * behind this method is that I've found that the JTable.print() method doesn't
 * work well with OSX and Linux distributions. This method renders a cross
 * platform print dialog and applies some basic formatting to the printing job
 *
 * @author Fede
 */
public final class JTablePrinter {

    public final static MessageFormat HEADER_FORMAT = new MessageFormat("");
    public final static MessageFormat FOOTER_FORMAT = new MessageFormat("");

    public static void print(JTable jTable) throws PrinterException {
        print(jTable,
                JTable.PrintMode.FIT_WIDTH,
                FOOTER_FORMAT,
                HEADER_FORMAT,
                PageFormat.LANDSCAPE);
    }

    public static void print(JTable jTable, JTable.PrintMode printMode,
            MessageFormat headerFormat, MessageFormat footerFormat, int orientation) throws PrinterException {
        if (jTable == null) {
            throw new NullPointerException("jTable is null");
        }
        Printable printable = jTable.getPrintable(printMode, headerFormat, footerFormat);
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        pageFormat.setOrientation(orientation);
        job.setPrintable(printable, pageFormat);
        if (job.printDialog()) {
            job.setJobName("JTable printer job");
            job.print();
        }

    }
}
