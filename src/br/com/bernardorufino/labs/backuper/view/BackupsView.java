package br.com.bernardorufino.labs.backuper.view;

import br.com.bernardorufino.labs.backuper.Application;
import br.com.bernardorufino.labs.backuper.controller.BackupsManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BackupsView {

    public static final String ERROR_MESSAGE = "Ocorreu um erro.";
    public static final String TITLE = "Backuper";

    public static final int WIDTH = 580;
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
    private JButton addOriginButton;
    private JList clientFoldersList;

    private String backupsFolder;
    private String clientFolder;
    private JFileChooser chooser;
    private String folderChosen;
    private HistoryAdapter historyModel;
    private DefaultListModel<String> clientFoldersListModel;
    private BackupsManager manager;
    private boolean uiLocked = false;

    public BackupsView() {
        manager = Application.controller;
        build();
        setListeners();
    }

    public void build() {

        // clientFoldersList setup with default model
        clientFoldersListModel = new DefaultListModel<>();
        clientFoldersList.setModel(clientFoldersListModel);
        clientFoldersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

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
//        backupsFolder = "D:\\Backuper\\Backup";
//        clientFolder = "D:\\Backuper\\Client";
//        backupsFolderField.setText(backupsFolder);
//        clientFolderField.setText(clientFolder);
//        // History table running
//        // Needs to be after the window creation to set columns width properly
//        setUpController();

    }

    private void setUpHistoryTable() {
        historyModel = new HistoryAdapter(Application.controller.getBackups());
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

    private void fetchClientFolders() {
        try {
            manager.fetchBackups();
        } catch (IOException err) {
            JOptionPane.showMessageDialog(frame, ERROR_MESSAGE);
            err.printStackTrace();
        }
        updateClientFoldersList();
    }

    private void updateClientFoldersList() {
        clientFoldersListModel.clear();
        for (File folder : manager.getClientFolders()) {
            clientFoldersListModel.addElement(folder.getAbsolutePath());
        }
    }

    private void updateButtonsState() {
        if (uiLocked) return;
        chooseOrigin.setEnabled(!manager.hasBackups());
        addOriginButton.setEnabled(!manager.hasBackups());
        restoreButton.setEnabled(
                manager.hasBackups()
                && (history.getSelectedRows().length == 1)
                && (clientFoldersList.getSelectedIndices().length > 0)
        );
        makeBackupButton.setEnabled(manager.getClientFolders().size() > 0);
        chooseDestination.setEnabled(true);
    }

    private void disableCriticalActionButtons() {
        chooseDestination.setEnabled(false);
        addOriginButton.setEnabled(false);
        makeBackupButton.setEnabled(false);
        restoreButton.setEnabled(false);
    }

    private List<File> getSelectedClientFolders() {
        List<File> clientFolders = manager.getClientFolders();
        List<File> selection = new ArrayList<>();
        for (int i : clientFoldersList.getSelectedIndices()) {
            selection.add(clientFolders.get(i));
        }
        return selection;
    }

    private void setListeners() {

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F5:
                        Application.execute(new Runnable() {
                            public void run() {
                                startProgressBar();
                                fetchClientFolders();
                                updateButtonsState();
                                setUpHistoryTable();
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
                updateButtonsState();
            }
        });

        chooseDestination.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (folderChooser("Escolha uma pasta destino")) {
                    backupsFolderField.setText(folderChosen);
                    backupsFolder = folderChosen;
                    Application.execute(new Runnable() {
                        public void run() {
                            disableCriticalActionButtons();
                            startProgressBar();
                            manager.setBackupsFolder(new File(backupsFolder));
                            fetchClientFolders();
                            setUpHistoryTable();
                            updateButtonsState();
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
                }
            }
        });

        addOriginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File folder = new File(clientFolder);
                if (folder.exists()) {
                    manager.addClientFolder(folder);
                } else {
                    JOptionPane.showMessageDialog(frame, ERROR_MESSAGE);
                }
                updateButtonsState();
                updateClientFoldersList();
            }
        });

        makeBackupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Application.execute(new Runnable() {
                    public void run() {
                        disableCriticalActionButtons();
                        startProgressBar();
                        try {
                            manager.makeBackup();
                        } catch (IOException err) {
                            JOptionPane.showMessageDialog(frame, ERROR_MESSAGE);
                            err.printStackTrace();
                        } finally {
                            updateButtonsState();
                            setUpHistoryTable();
                            finishProgressBar();
                        }
                    }
                });
            }
        });

        restoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final int row = history.getSelectedRow();
                String versionName = (String) historyModel.getValueAt(row, 0);
                int choice = JOptionPane.showConfirmDialog(frame,
                        "Tem certeza que deseja restaurar a versão " + versionName, null, JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) return;
                Application.execute(new Runnable() {
                    public void run() {
                        disableCriticalActionButtons();
                        startProgressBar();
                        try {
                            String id = historyModel.getID(row);
                            List<File> clientFolders = getSelectedClientFolders();
                            manager.restore(id, clientFolders);
                        } catch (IOException err) {
                            JOptionPane.showMessageDialog(frame, ERROR_MESSAGE);
                            err.printStackTrace();
                        } finally {
                            updateButtonsState();
                            setUpHistoryTable();
                            finishProgressBar();
                        }

                    }
                });
            }
        });

    }

}
