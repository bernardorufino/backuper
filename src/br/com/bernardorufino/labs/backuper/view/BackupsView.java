package br.com.bernardorufino.labs.backuper.view;

import br.com.bernardorufino.labs.backuper.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


public class BackupsView {

    public static final String TITLE = "Backuper";
    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;
    public static final String lookAndFeel = UIManager.getSystemLookAndFeelClassName();

    // Needs to use this method in order to call setLookAndFeel before UI being created
    public static BackupsView create() {
        try { UIManager.setLookAndFeel(lookAndFeel); } catch (Exception e) { /* Empty */ }
        return new BackupsView();
    }

    // Make Backup
    private JTextField backupsFolderField;
    private JButton fetchHistory;
    private JTextField clientFolderField;
    private JButton chooseDestination;
    private JButton chooseOrigin;
    private JButton makeBackup;
    private JProgressBar backupProgress;
    private JTable history;

    // Restore
    private JTextField restoreFromDisplay;
    private JButton chooseRestoreFromButton;
    private JTextArea historySelect;
    private JButton makeRestore;
    private JProgressBar restoreProgress;

    private JFrame frame;
    private JPanel main;
    private HistoryAdapter historyModel;

    public BackupsView() {
        setListeners();
        //setModels();
    }

//    private void setModels() {
//        historyModel = controller.getHistoryAdapter();
//        history.setModel(historyModel);
//    }

    public void build() {
        //TODO: Put in another thread

        // Progress bar configurations
        backupProgress.setMinimum(0);
        backupProgress.setMaximum(10);
        backupProgress.setValue(0);

        // Create the window and display
        frame = new JFrame(TITLE);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void setUpController() {
        String clientFolder = clientFolderField.getText();
        String backupsFolder = backupsFolderField.getText();
        try {
            Application.controller.setUp(backupsFolder, clientFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setUpHistoryTable();
    }

    private void setUpHistoryTable() {
        historyModel = Application.controller.getHistoryAdapter();
        history.setModel(historyModel);
        history.getColumn(historyModel.getColumnName(0)).setMinWidth((int) (history.getWidth() * 0.75));
    }

    private void startBackupProgressBar() {
        backupProgress.setIndeterminate(true);
    }

    private void finishBackupProgressBar() {
        backupProgress.setIndeterminate(false);
    }

    private void setListeners() {

        fetchHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.execute(new Runnable() {
                    public void run() {
                        startBackupProgressBar();
                        setUpController();
                        finishBackupProgressBar();
                    }
                });
            }
        });

        makeBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.execute(new Runnable() {
                    public void run() {
                        startBackupProgressBar();
                        setUpController();
                        try {
                            Application.controller.makeBackup();
                        } catch (IOException err) {
                            JOptionPane.showMessageDialog(frame, "Ocorreu um erro =(");
                        } finally {
                            setUpHistoryTable();
                            finishBackupProgressBar();
                        }
                    }
                });
            }
        });

    }

}
