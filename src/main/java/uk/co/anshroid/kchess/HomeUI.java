package uk.co.anshroid.kchess;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;

/**
 * The home UI for the app
 */
public class HomeUI extends JPanel {
    public JButton player1Name;
    public JButton player2Name;

    public JButton[] TCButtons;

    /**
     * Create a new HomeUI
     * @param exitHook The exit hook to call when the app is exited
     */
    public HomeUI(MainScreen.ExitHook exitHook, String savePath, MainScreen.LoadHook loadHook, MainScreen.LoadNewHook loadNewHook) {
        super();

        setLayout(new BorderLayout());

        //region Title
        Box title = Box.createVerticalBox();

        add(title, BorderLayout.PAGE_START);

        JLabel titleText = new JLabel("KChess");
        titleText.setFont(new Font("Arial", Font.BOLD, 48));
        titleText.setAlignmentX(CENTER_ALIGNMENT);
        title.add(titleText);

        JLabel subtitleText = new JLabel("by Anshroid");
        subtitleText.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleText.setAlignmentX(CENTER_ALIGNMENT);
        title.add(subtitleText);

        JLabel websiteText = new JLabel("https://anshroid.github.io/kchess");
        websiteText.setFont(new Font("Arial", Font.PLAIN, 12));
        websiteText.setAlignmentX(CENTER_ALIGNMENT);
        title.add(websiteText);

        title.add(Box.createVerticalStrut(title.getMaximumSize().height));
        //endregion

        JPanel content = new JPanel(new GridLayout(1, 3));

        //region Load
        Box load = Box.createVerticalBox();
        content.add(load);

        JButton up = new JButton("↑");
        up.setAlignmentX(Component.CENTER_ALIGNMENT);
        load.add(up);

        for (File file : Objects.requireNonNull(new File(savePath).listFiles())) {
            String fn = file.getName();
            if (!fn.endsWith(".kchess")) continue;

            JButton button = new JButton(fn.split("\\.")[0]);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(e -> loadHook.load(file));
            load.add(button);
        }

        JButton down = new JButton("↓");
        down.setAlignmentX(Component.CENTER_ALIGNMENT);
        load.add(down);

        load.setVisible(false);
        //endregion

        //region Buttons
        Box buttons = Box.createVerticalBox();
        content.add(buttons);

        JButton newGame = new JButton("New...");
        newGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.add(newGame);

        JButton continueGame = new JButton("Continue");
        continueGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueGame.addActionListener(e -> loadHook.load(new File(savePath, "kchess.sav")));
        buttons.add(continueGame);

        JButton loadGame = new JButton("Load...");
        loadGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.add(loadGame);

        JButton exit = new JButton("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.addActionListener(e -> exitHook.exit());
        buttons.add(exit);
        //endregion

        //region New
        JPanel newGamePanel = new JPanel(new GridLayout(2, 1));
        content.add(newGamePanel);

        //region Names
        JPanel names = new JPanel(new GridLayout(3, 3));
        newGamePanel.add(names);

        for (JButton button : new JButton[] {
            new JButton("A"), new JButton("B"), new JButton("V"),
            new JButton("M"), new JButton("J"), new JButton("H"),
            new JButton("S"), new JButton("P"), new JButton("G")
        }) {
            names.add(button);
            button.addActionListener(e -> {
                button.setEnabled(false);
                if (player1Name != null) player1Name.setEnabled(true);
                player1Name = player2Name;
                player2Name = button;

                if (player1Name != null) {
                    for (JButton button1 : TCButtons) {
                        button1.setEnabled(true);
                    }
                }
            });
        }
        //endregion

        //region Times
        Box times = Box.createVerticalBox();
        newGamePanel.add(times);

        times.add(Box.createVerticalStrut(times.getMaximumSize().height));

        TCButtons = new JButton[] {new JButton("∞"), new JButton("10+0"), new JButton("5+0"), new JButton("3+2")};
        for (JButton button : TCButtons) {
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            times.add(button);
            button.setEnabled(false);
            button.addActionListener(e -> loadNewHook.loadNew(player1Name.getText(), player2Name.getText()));
        }
        //endregion

        newGamePanel.setVisible(false);
        //endregion

        // Set up the UI
        add(content, BorderLayout.CENTER);
        newGame.addActionListener(e -> {load.setVisible(false); newGamePanel.setVisible(true);});
        loadGame.addActionListener(e -> {load.setVisible(true); newGamePanel.setVisible(false);});
    }
}
