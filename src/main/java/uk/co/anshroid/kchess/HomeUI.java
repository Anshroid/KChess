package uk.co.anshroid.kchess;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * The home UI for the app
 */
public class HomeUI extends JPanel {
    /**
     * Create a new HomeUI
     * @param exitHook The exit hook to call when the app is exited
     */
    public HomeUI(MainScreen.ExitHook exitHook, String savePath, MainScreen.LoadHook loadHook) {
        super();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        //region Title
        Box title = Box.createVerticalBox();
        c.gridx = 0;
        c.gridy = 0;
        layout.setConstraints(title, c);

        JLabel titleText = new JLabel("KChess");
        titleText.setFont(new Font("Arial", Font.BOLD, 48));
        title.add(titleText);

        JLabel subtitleText = new JLabel("by Anshroid");
        subtitleText.setFont(new Font("Arial", Font.PLAIN, 12));
        title.add(subtitleText);

        JLabel websiteText = new JLabel("https://anshroid.github.io/kchess");
        websiteText.setFont(new Font("Arial", Font.PLAIN, 12));
        title.add(websiteText);

        title.add(Box.createVerticalStrut(title.getMaximumSize().height / 2));
        add(title);
        //endregion

        //region Main
        JPanel main = new JPanel();
        c.gridx = 0;
        c.gridy = 1;
        layout.setConstraints(main, c);
        add(main);


        GridBagLayout mainLayout = new GridBagLayout();
        GridBagConstraints mainC = new GridBagConstraints();

        //region Buttons
        Box buttons = Box.createVerticalBox();
        mainC.gridx = 1;
        mainC.gridy = 0;
        mainLayout.setConstraints(buttons, mainC);
        main.add(buttons);

        JButton newGame = new JButton("New...");
        newGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.add(newGame);

        JButton loadGame = new JButton("Continue");
        loadGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadGame.addActionListener(e -> loadHook.load(new File(savePath, "kchess.sav")));
        buttons.add(loadGame);

        JButton settings = new JButton("Load...");
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.add(settings);

        JButton exit = new JButton("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.addActionListener(e -> exitHook.exit());
        buttons.add(exit);
        //endregion

        //endregion

        // Set up the UI
        this.setLayout(layout);
    }
}
