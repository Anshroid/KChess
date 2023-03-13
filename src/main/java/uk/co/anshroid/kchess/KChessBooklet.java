package uk.co.anshroid.kchess;

import com.amazon.kindle.booklet.AbstractBooklet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

public class KChessBooklet extends AbstractBooklet implements ActionListener {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.logFile","/mnt/us/kchess.log");
        System.setProperty("org.slf4j.simpleLogger.showDateTime","true");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName","true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat","yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        log = LoggerFactory.getLogger(KChessBooklet.class);
        Util.setKindle(true);
    }

    private static final Logger log;

    private Container rootContainer = null;

    public KChessBooklet() {
		new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    public void run() {
                        KChessBooklet.this.longStart();
                    }
                },
                1000
        );
	}

    @Override
    public void b(URI uri) {
        log.info("start called with {} ", uri);
        super.b(uri);
    }

    // Because this got obfuscated...
	private Container getUIContainer() {
		// Check our cached value, first
		if (rootContainer != null) {
			return rootContainer;
		} else {
			try {
				Container container = Util.getUIContainer(this);
				if (container == null) {
                    log.error("Failed to find getUIContainer method, abort!");
                    endBooklet();
					return null;
				}
				rootContainer = container;
				return container;
			} catch (Throwable t) {
				throw new RuntimeException(t.toString());

			}
		}
	}

	private void endBooklet() {
		try {
			// Send a BACKWARD lipc event to background the app (-> stop())
			// NOTE: This has a few side effects, since we effectively skip create & longStart
			//	 on subsequent start-ups, and we (mostly) never go to destroy().
			// NOTE: Incidentally, this is roughly what the [Home] button does on the Touch, so, for the same reason,
			//	 it's recommended not to tap Home on that device ;).
			// NOTE: Setting the unloadPolicy to unloadOnPause in the app's appreg properties takes care of that,
			//	 stop() then *always* leads to destroy() :).
			//Runtime.getRuntime().exec("lipc-set-prop com.lab126.appmgrd backward 0");
            log.info("Ending Booklet");
            //noinspection deprecation
			Runtime.getRuntime().exec("lipc-set-prop com.lab126.appmgrd stop app://uk.co.anshroid.kchess");
		} catch (IOException e) {
//			log.error("Failed when terminating ", e);
		}
	}

	private void longStart() {
		try {
			initializeUI(); // step 3
		} catch (Throwable t) {
            log.error(t.getMessage(), new RuntimeException(t));
            endBooklet();
			throw new RuntimeException(t);
		}
	}

	private void initializeUI() {
        log.debug("Starting Up");
		Container root = getUIContainer();


        log.debug("Starting Up1 {} ", root);
        assert root != null;
        log.debug("Component Count {} ", root.getComponentCount());
        log.debug("Components {} ", Arrays.toString(root.getComponents()));
        log.debug("Starting Up1 {} ", root);



        log.debug("Starting Up2");
        Font rootFont = new Font("Futura",Font.PLAIN, 10);
        root.setFont(rootFont);

		MainScreen mainScreen = new MainScreen(root, KChessBooklet.this::endBooklet, "/mnt/us/");
        listComponentTree(root,"->");
		mainScreen.start();
        // main screen is now referenced by Swing and a Thread.
        // no reference held here.
	}

    public void listComponentTree(Container root, String indent) {
        log.debug("{} ------------------------------", indent);
        for(Component c : root.getComponents()) {
            log.debug("{} {} ", indent, c);
            if ( c instanceof Container ) {
                listComponentTree((Container) c, indent+"->");
            }
        }
        log.debug("{} ------------------------------", indent);
    }

	public void destroy() {
		//new Logger().append("destroy()");
		// Try to cleanup behind us on exit...
		try {
			// NOTE: This can be a bit racey with stop(),
			//	 so sleep for a tiny bit so our commandToRunOnExit actually has a chance to run...
			Thread.sleep(175);
			Util.updateCCDB("KChess", "/mnt/us/documents/KChess.kchess");
		} catch (Exception ignored) {
			// Avoid the framework shouting at us...
		}

		super.destroy();
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("Action Performed {} ", e);
    }
}
