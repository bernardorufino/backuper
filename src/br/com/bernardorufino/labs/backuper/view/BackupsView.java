package br.com.bernardorufino.labs.backuper.view;

import br.com.bernardorufino.labs.backuper.Application;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;


public class BackupsView {

    public static final String ERROR_MESSAGE = "Ocorreu um erro.";
    public static final String TITLE = "Backuper";

    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;
    public static final String lookAndFeel = UIManager.getSystemLookAndFeelClassName();

    // Needs to use this method in order to call setLookAndFeel before UI being created
    public static BackupsView create() {
        try { UIManager.setLookAndFeel(lookAndFeel); } catch (Exception e) { /* Empty */ }
        return new BackupsView();
    }

    private JFrame frame;
    private JPanel main;
    private JTextField backupsFolderField;
    private JTextField clientFolderField;
    private JButton makeBackupButton;
    private JProgressBar progressBar;
    private JTable history;
    private JButton chooseDestination;
    private JButton chooseOrigin;
    private JButton restoreButton;

    private String backupsFolder;
    private String clientFolder;
    private JFileChooser chooser;
    private String folderChosen;
    private HistoryAdapter historyModel;

    public BackupsView() {
        build();
        setListeners();
    }

    public void build() {

        // File chooser setup
        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText("Selecionar");

        // Progress bar configurations
        progressBar.setMinimum(0);
        progressBar.setMaximum(10);
        progressBar.setValue(0);

        // Create the window and display
        frame = new JFrame(TITLE);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // Comment below in production
        backupsFolder = "D:\\Backuper\\Backup";
        clientFolder = "D:\\Backuper\\Client";
        backupsFolderField.setText(backupsFolder);
        clientFolderField.setText(clientFolder);
        // History table running
        // Needs to be after the window creation to set columns width properly
        setUpController();

    }

    private void setUpController() {
        if (backupsFolder == null || clientFolder == null) return;
        try {
            Application.controller.setUp(backupsFolder, clientFolder);
            makeBackupButton.setEnabled(true);
        } catch (IOException e) {
            makeBackupButton.setEnabled(false);
            JOptionPane.showMessageDialog(frame, ERROR_MESSAGE);
        }
        setUpHistoryTable();
    }

    private void setUpHistoryTable() {
        historyModel = Application.controller.getHistoryAdapter();
        history.setModel(historyModel);
        // Change below to INTERVAL_SELECTION when merge is ready
        history.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        history.getColumn(historyModel.getColumnName(0)).setMinWidth((int) (history.getWidth() * 0.75));
    }

    private void startProgressBar() { progressBar.setIndeterminate(true); }
    private void finishProgressBar() { progressBar.setIndeterminate(false); }

    private boolean folderChooser(String title) {
        chooser.setDialogTitle(title);
        boolean chosen = chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION;
        folderChosen = (chosen) ? chooser.getSelectedFile().getAbsolutePath() : null;
        return chosen;
    }

    private void setListeners() {

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F5:
                        Application.execute(new Runnable() {
                            public void run() {
                                startProgressBar();
                                setUpController();
                                finishProgressBar();
                            }
                        });
                        break;
                }
                return false;
            }
        });

        history.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                restoreButton.setEnabled(history.getSelectedRows().length == 1);
            }
        });

        chooseDestination.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (folderChooser("Escolha uma pasta destino")) {
                    backupsFolderField.setText(folderChosen);
                    backupsFolder = folderChosen;
                    Application.execute(new Runnable() {
                        public void run() {
                            startProgressBar();
                            setUpController();
                            finishProgressBar();
                        }
                    });
                }
            }
        });

        chooseOrigin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (folderChooser("Escolha uma pasta de origem")) {
                    clientFolderField.setText(folderChosen);
                    clientFolder = folderChosen;
                    Application.execute(new Runnable() {
                        public void run() {
                            startProgressBar();
                            setUpController();
                            finishProgressBar();
                        }
                    });
                }
            }
        });

        makeBackupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.execute(new Runnable() {
                    public void run() {
                        startProgressBar();
                        try {
                            Application.controller.makeBackup();
                        } catch (IOException err) {
                            JOptionPane.showMessageDialog(frame, ERROR_MESSAGE);
                            err.printStackTrace();
                        } finally {
                            setUpHistoryTable();
                            finishProgressBar();
                        }
                    }
                });
            }
        });

        restoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final int backupIndex = history.getSelectedRow();
                String versionName = (String) historyModel.getValueAt(backupIndex, 0);
                int choice = JOptionPane.showConfirmDialog(frame,
                        "Tem certeza que deseja restaurar a versão " + versionName, null, JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
                Application.execute(new Runnable() {
                    public void run() {
                        startProgressBar();
                        try {
                            Application.controller.restore(backupIndex);
                        } catch (IOException err) {
                            JOptionPane.showMessageDialog(frame, ERROR_MESSAGE);
                            err.printStackTrace();
                        } finally {
                            setUpHistoryTable();
                            finishProgressBar();
                        }

                    }
                });
            }
        });

    }

}
