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

    public interface LoadNewHook {
        void loadNew(String player1Name, String player2Name);
    }

    public MainScreen(Container root, ExitHook exitHook, String savePath) {
        this.root = root;
        mainExitHook = exitHook;
        MainScreen.savePath = savePath;

        //noinspection ResultOfMethodCallIgnored
        new File(savePath).mkdirs();

        if (!Util.isKindle()) {
            // Use a default screen size for desktop
            root.setSize(new Dimension(536, 721));
        }


        currentUI = new HomeUI(exitHook, savePath, this::LoadMainUI, this::LoadMainUINew);
        root.add(currentUI);
        root.validate();
        root.setVisible(true);
    }

    public void start() {
        log.info("Starting");
        log.info("Started");
    }

    public void LoadMainUINew(String player1Name, String player2Name) {
        root.remove(currentUI);
        currentUI = new MainUI(() -> {
            root.remove(currentUI);
            currentUI = new HomeUI(mainExitHook, savePath, this::LoadMainUI, this::LoadMainUINew);
            root.add(currentUI);
            root.validate();
        }, new Board(root.getSize(), player1Name, player2Name));
        root.add(currentUI);
        root.validate();
    }

    public void LoadMainUI(File saveFile) {
        root.remove(currentUI);
        currentUI = new MainUI(() -> {
            root.remove(currentUI);
            currentUI = new HomeUI(mainExitHook, savePath, this::LoadMainUI, this::LoadMainUINew);
            root.add(currentUI);
            root.validate();
        }, Board.load(saveFile, root.getSize()));
        root.add(currentUI);
        root.validate();
    }
}
