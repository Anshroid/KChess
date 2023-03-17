package uk.co.anshroid.kchess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class MainScreen {

    private static final Logger log = LoggerFactory.getLogger(MainScreen.class);

    public static String savePath;

    private JPanel currentUI;
    private final Container root;

    private final ExitHook mainExitHook;

    public interface ExitHook {
        void exit();
    }
    
    public interface LoadHook {
        void load(File saveFile);
    }

    public MainScreen(Container root, ExitHook exitHook, String savePath) {
        this.root = root;
        mainExitHook = exitHook;
        MainScreen.savePath = savePath;

        if (!Util.isKindle()) {
            // Use a default screen size for desktop
            root.setSize(new Dimension(536, 721));
        }


        currentUI = new HomeUI(exitHook, savePath, this::LoadMainUI);
        root.add(currentUI);
        root.validate();
        root.setVisible(true);
    }

    public void start() {
        log.info("Starting");
        log.info("Started");
    }

    public void LoadMainUI() {
        root.remove(currentUI);
        currentUI = new MainUI(() -> {
            root.remove(currentUI);
            currentUI = new HomeUI(mainExitHook, savePath, this::LoadMainUI);
            root.add(currentUI);
            root.validate();
        }, new Board(root.getSize()));
        root.add(currentUI);
        root.validate();
    }

    public void LoadMainUI(File saveFile) {
        LoadMainUI();
        ((MainUI) currentUI).board.load(saveFile);
    }
}
