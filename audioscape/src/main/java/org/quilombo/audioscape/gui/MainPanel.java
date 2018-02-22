package org.quilombo.audioscape.gui;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JFrame {

    Color _background = Color.BLACK;
    Color _foreground = Color.WHITE;

    private double _screenWidth;
    private double _screenHeight;

    JPanel backgroundPanel;

    public MainPanel() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        _screenWidth = screenSize.getWidth();
        _screenHeight = screenSize.getHeight();
    }

    public void showFull() {
        backgroundPanel = new JPanel(new BorderLayout());
        getContentPane().add(backgroundPanel);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setVisible(true);
    }

    public void addVideo() {

    }

    public void clearScreen(Color color) {
        backgroundPanel.removeAll();
        backgroundPanel.setBackground(color);
        invalidate();
        repaint();
    }

    public void showText(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Serif", Font.BOLD, size));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setForeground(_foreground);
        backgroundPanel.add(label);
        invalidate();
        repaint();
    }


    public static void main(String[] args) throws InterruptedException {
        MainPanel main = new MainPanel();
        main.showFull();
        main.clearScreen(Color.red);
        //main.showText("GRAVANDO", 150, Color.white);
        main.addVideo();
        while (true) {
            Thread.sleep(1000);
        }
    }


}
