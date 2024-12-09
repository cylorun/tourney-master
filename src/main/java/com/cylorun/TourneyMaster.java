package com.cylorun;

import com.cylorun.gui.TourneyMasterWindow;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class TourneyMaster {
    public static final String VERSION = TourneyMaster.class.getPackage().getImplementationVersion() == null ? "DEV" : TourneyMaster.class.getPackage().getImplementationVersion();

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatDarculaLaf());

        log(Level.INFO, "Running Tourney-Master v" + VERSION);

        TourneyMasterWindow.getInstance();
    }

    public static void log(Level level, Object s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        String time = formatter.format(LocalDateTime.now());

        if (level.equals(Level.INFO)) {
            System.out.printf("[%s/%s] %s%n", "INFO", time, s.toString());
        } else if (level.equals(Level.WARNING)) {
            System.out.printf("[%s/%s] %s%n", "WARNING", time, s.toString());
        } else if (level.equals(Level.SEVERE)) {
            System.err.printf("[%s/%s] %s%n", "SEVERE", time, s.toString());
        }
    }
}