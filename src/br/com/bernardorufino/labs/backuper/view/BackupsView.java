package br.com.bernardorufino.labs.backuper.view;

import br.com.bernardorufino.labs.backuper.controller.BackupsController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BackupsView {

    public static final String TITLE = "Backuper";
    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;

    // Needs to use this method in order to call setLookAndFeel before UI being created
    public static BackupsView create(BackupsController controller) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { /* Empty */ }
        return new BackupsView(controller);
    }

    // Make Backup
    private JTextField backupsFolderField;
    private JButton fetchHistory;
    private JTextField clientFolderField;
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

    private JPanel main;
    private final BackupsController controller;

    public BackupsView(BackupsController controller) {
        this.controller = controller;
        setListeners();
        build();
    }

    public void build() {
        //TODO: Put in another thread
        JFrame frame = new JFrame(TITLE);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void setUpController() {
        String clientFolder = clientFolderField.getText();
        String backupsFolder = backupsFolderField.getText();
        controller.setUp(clientFolder, backupsFolder);
    }

    private void setListeners() {

        fetchHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //controller.fetchHistoryList();
            }
        });

        makeBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUpController();
                controller.makeBackup();
            }
        });

    }

}
