package uk.co.anshroid.kchess;

import javax.swing.*;
import java.awt.*;

/**
 * The main UI for the app
 */
public class MainUI extends JPanel {
    public final Board board;
    public final Timer Player2Timer;
    public final Timer Player1Timer;

    public final JButton[] promotionButtons;

    public static Dimension size;

    /**
     * Interface for the exit hook
     */
    public interface MainUIExit {
        void exit();
    }

    /**
     * Create a new MainUI
     * @param root The root container to add this to
     * @param exitHook The exit hook to call when the app is exited
     */
    public MainUI(Container root, MainUIExit exitHook, String savePath) {
        super();

        if (!Util.isKindle()) {
            // Use a default screen size for desktop
            root.setSize(new Dimension(536, 721));
        }

        size = root.getSize();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        // Add the board to the middle of the screen as fullwidth
        board = new Board();
        board.load(savePath + "kchess.sav");
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 1;
        layout.setConstraints(board, c);
        add(board);

        // Add the timers to the right of the screen on the top and bottom
        Player2Timer = new Timer();
        c.gridx = 1;
        c.gridwidth = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        layout.setConstraints(Player2Timer, c);
        add(Player2Timer);

        Player1Timer = new Timer();
        c.gridx = 1;
        c.gridy = 2;
        layout.setConstraints(Player1Timer, c);
        add(Player1Timer);

        // Add promotions to the left of the screen above the board
        JPanel promotions = new JPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(promotions, c);
        add(promotions);

        promotionButtons = new JButton[] {new JButton("♛"), new JButton("♜"), new JButton("♝"), new JButton("♞")};
        for (JButton button : promotionButtons) {
            button.addActionListener(e -> {
                board.promote(button.getText());
                for (JButton b : promotionButtons) {
                    b.setEnabled(false);
                }
            });
            button.setEnabled(false);
            promotions.add(button);
        }

        // Add controls to the left of the screen under the board
        JPanel controls = new JPanel();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(controls, c);
        add(controls);

        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> {board.save(savePath + "kchess.sav"); exitHook.exit();});
        controls.add(exit);

        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> board.resetBoard());
        controls.add(reset);

        JButton back = new JButton("Back");
        back.addActionListener(e -> board.movePast());
        controls.add(back);

        JButton forward = new JButton("Forward");
        forward.addActionListener(e -> board.moveFuture());
        controls.add(forward);

        JButton save = new JButton("Save");
        save.addActionListener(e -> { String fn = Util.rollSaveFilename(savePath, "out"); board.save(fn); });
        controls.add(save);

        // Set up the UI
        this.setLayout(layout);
        root.validate();
        root.add(this);
    }

    /**
     * Set the timer text after the game has been won
     * @param side The side that won the game
     */
    public void endGame(Boolean side) {
        (side ? Player2Timer : Player1Timer).setText("WINNER");
        (side ? Player1Timer : Player2Timer).setText("GGWP");
    }

    /**
     * Show the promotion menu
     */
    public void showPromotionMenu() {
        for (JButton button : promotionButtons) {
            button.setEnabled(true);
        }
    }
}
