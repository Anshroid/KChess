package uk.co.anshroid.kchess;

import javax.swing.*;


/**
 * Created by ieb on 06/06/2020.
 * Adapted by Anshroid on 26/01/2023
 */
public class Main {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.logFile","System.err");
        System.setProperty("org.slf4j.simpleLogger.showDateTime","true");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName","true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat","yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    public static void main(String[] args) {
        Util.setKindle(false); // Kindle AWT implementation has some non-standard behaviour.

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        new MainScreen(frame, () -> System.exit(0), "/home/anshroid/kchess.sav");
    }
}

