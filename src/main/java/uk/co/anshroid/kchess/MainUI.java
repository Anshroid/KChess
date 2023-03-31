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
        c.gridwidth = 3;
        c.gridy = 1;
        layout.setConstraints(board, c);
        add(board);
        //endregion

        //region Player Names
        JLabel Player2Name = new JLabel(board.player2);
        Player2Name.setFont(new Font("Arial", Font.BOLD, 20));
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 0;
        layout.setConstraints(Player2Name, c);
        add(Player2Name);

        JLabel Player1Name = new JLabel(board.player1);
        Player1Name.setFont(new Font("Arial", Font.BOLD, 20));
        c.gridy = 2;
        layout.setConstraints(Player1Name, c);
        add(Player1Name);
        //endregion

        //region Timers
        // Add the timers to the right of the screen on the top and bottom
        Player2Timer = new Timer(Board.rootSize);
        c.gridx = 2;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        layout.setConstraints(Player2Timer, c);
        add(Player2Timer);

        Player1Timer = new Timer(Board.rootSize);
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
     * Set the metadata for the game after it has ended
     * @param reason The reason for the game ending
     */
    public void endGame(EndGameReason reason) {
        switch (reason) {
            case WHITE_WIN:
                Player1Timer.setText("WINNER");
                Player2Timer.setText("LOSER");
                break;
            case BLACK_WIN:
                Player1Timer.setText("LOSER");
                Player2Timer.setText("WINNER");
                break;
            case STALEMATE:
                Player1Timer.setText("STALEMATE");
                Player2Timer.setText("STALEMATE");
                break;
        }
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
