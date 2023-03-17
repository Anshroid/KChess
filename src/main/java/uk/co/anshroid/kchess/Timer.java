package uk.co.anshroid.kchess;

import javax.swing.*;
import java.awt.*;

public class Timer extends JButton {
    public Timer(Dimension rootSize) {
        super();
        setPreferredSize(new Dimension(rootSize.width / 5, rootSize.height / 14));
        setText("5:00");
        setEnabled(false);
    }
}
