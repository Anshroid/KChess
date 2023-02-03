package uk.co.anshroid.kchess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Created by ieb on 20/06/2020.
 */
public class MainScreen {

    private static final Logger log = LoggerFactory.getLogger(MainScreen.class);

    public interface MainScreenExit {
        void exit();
    }

    public MainScreen(Container root, MainScreenExit exitHook, String savePath) {
        new MainUI(root, exitHook::exit, savePath);

        root.validate();
        root.setVisible(true);
    }

    public void start() {
        log.info("Starting");
        log.info("Started");
    }
}
