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

    /**
     * Create a new MainUI
     * @param exitHook The exit hook to call when the app is exited
     */
    public MainUI( MainScreen.ExitHook exitHook, Board board) {
        super();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        //region Board
        // Add the board to the middle of the screen as fullwidth
        this.board = board;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 1;
        layout.setConstraints(board, c);
        add(board);
        //endregion

        //region Timers
        // Add the timers to the right of the screen on the top and bottom
        Player2Timer = new Timer(Board.rootSize);
        c.gridx = 1;
        c.gridwidth = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        layout.setConstraints(Player2Timer, c);
        add(Player2Timer);

        Player1Timer = new Timer(Board.rootSize);
        c.gridx = 1;
        c.gridy = 2;
        layout.setConstraints(Player1Timer, c);
        add(Player1Timer);
        //endregion

        //region Promotions
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
        //endregion

        //region Controls
        // Add controls to the left of the screen under the board
        JPanel controls = new JPanel();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(controls, c);
        add(controls);

        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> {board.save(); exitHook.exit();});
        controls.add(exit);

        JButton back = new JButton("Back");
        back.addActionListener(e -> board.movePast());
        controls.add(back);

        JButton forward = new JButton("Forward");
        forward.addActionListener(e -> board.moveFuture());
        controls.add(forward);
        //endregion

        // Set up the UI
        this.setLayout(layout);
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
