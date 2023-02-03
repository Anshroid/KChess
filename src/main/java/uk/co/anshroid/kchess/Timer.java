package uk.co.anshroid.kchess;

import javax.swing.*;
import java.awt.*;

public class Timer extends JButton {
    public Timer() {
        super();
        setPreferredSize(new Dimension(MainUI.size.width / 5, MainUI.size.height / 14));
        setText("5:00");
        setEnabled(false);
    }
}
