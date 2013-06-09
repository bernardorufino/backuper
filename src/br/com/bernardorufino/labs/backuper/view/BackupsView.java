package br.com.bernardorufino.labs.backuper.view;

import javax.swing.*;
import java.awt.*;

public class BackupsView {

    public static final String TITLE = "Backuper";
    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;

    private JPanel main;

    // Make Backup
    private JTextField destinationDisplay;
    private JTextField originDisplay;
    private JButton chooseDestination;
    private JButton chooseOrigin;
    private JTextArea history;
    private JButton makeBackup;
    private JProgressBar backupProgress;

    // Restore
    private JTextField restoreFromDisplay;
    private JButton chooseRestoreFromButton;
    private JTextArea historySelect;
    private JButton makeRestore;
    private JProgressBar restoreProgress;

    public static void main(String[] args) {
        build();
    }

    public static void build() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { /* Empty */ }
        JFrame frame = new JFrame(TITLE);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setContentPane(new BackupsView().main);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
