package br.com.bernardorufino.labs.backuper.view;

import br.com.bernardorufino.labs.backuper.Application;
import br.com.bernardorufino.labs.backuper.controller.BackupsManager;
import br.com.bernardorufino.labs.backuper.controller.BackupsTransaction;
import br.com.bernardorufino.labs.backuper.controller.Transaction;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


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
    private JButton makeBackupButton;
    private JProgressBar progressBar;
    private JTable history;
    private JButton chooseDestination;
    private JButton chooseOrigin;
    private JButton restoreButton;
    private JList clientFoldersList;
    private JButton cancelButton;

    private JFileChooser backupsFolderChooser;
    private JFileChooser clientFoldersChooser;
    private HistoryAdapter historyModel;
    private DefaultListModel<String> clientFoldersListModel;
    private BackupsManager manager;
    private BackupsTransaction transactions;
    private Future backgroundTask;

    public BackupsView() {
        manager = Application.controller;
        transactions = new BackupsTransaction(manager);
        build();
        setListeners();
    }

    public void build() {

        // clientFoldersList setup with default model
        clientFoldersListModel = new DefaultListModel<>();
        clientFoldersList.setModel(clientFoldersListModel);
        clientFoldersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Backups file chooser setup
        backupsFolderChooser = new JFileChooser();
        backupsFolderChooser.setMultiSelectionEnabled(false);
        backupsFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        backupsFolderChooser.setApproveButtonText("Selecionar");
        backupsFolderChooser.setDialogTitle("Escolha uma pasta destino");

        // Client folders file chooser setup
        clientFoldersChooser = new JFileChooser();
        clientFoldersChooser.setMultiSelectionEnabled(true);
        clientFoldersChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        clientFoldersChooser.setApproveButtonText("Selecionar");
        clientFoldersChooser.setDialogTitle("Escolha uma pasta de origem");

        // Progress bar configurations
        progressBar.setMinimum(0);
        progressBar.setMaximum(10);
        progressBar.setValue(0);

        // Create the window and display
        frame = new JFrame(TITLE);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setResizable(false);
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // Adjust table of backup versions
        historyModel = new HistoryAdapter(manager.getBackups());
        history.setModel(historyModel);
        history.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        history.getColumn(historyModel.getColumnName(0)).setMinWidth((int) (history.getWidth() * 0.75));

    }

    private void setUpHistoryTable() {
        historyModel.update(manager.getBackups());
        history.updateUI();
    }

    private void callUI(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException | InvocationTargetException e) {
                // Weird bug here, try to cancel while macking backup and uncomment
                // line below to see the exception
                // e.printStackTrace();
            }
        }
    }

    private void startProgressBar() {
        callUI(new Runnable() {
            public void run() {
                progressBar.setIndeterminate(true);
            }
        });
    }

    private void finishProgressBar() {
        callUI(new Runnable() {
            public void run() {
                progressBar.setIndeterminate(false);
            }
        });
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
        callUI(new Runnable() {
            public void run() {
                clientFoldersListModel.clear();
                for (File folder : manager.getClientFolders()) {
                    clientFoldersListModel.addElement(folder.getAbsolutePath());
                }
            }
        });
    }

    private void updateButtonsState() {
        callUI(new Runnable() {
            public void run() {
                chooseOrigin.setEnabled(!manager.hasBackups());
                restoreButton.setEnabled(
                        manager.hasBackups()
                                && (history.getSelectedRows().length == 1)
                                && (clientFoldersList.getSelectedIndices().length > 0)
                );
                makeBackupButton.setEnabled(manager.getClientFolders().size() > 0);
                chooseDestination.setEnabled(true);
                cancelButton.setEnabled(false);
            }
        });
    }

    private void disableCriticalActionButtons(final boolean canCancel) {
        callUI(new Runnable() {
            public void run() {
                cancelButton.setEnabled(canCancel);
                chooseDestination.setEnabled(false);
                makeBackupButton.setEnabled(false);
                restoreButton.setEnabled(false);
            }
        });
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
                        backgroundTask = Application.execute(new Runnable() {
                            public void run() {
                                startProgressBar();
                                fetchClientFolders();
                                updateButtonsState();
                                updateClientFoldersList();
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

        clientFoldersList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) { updateButtonsState(); }
        });

        clientFoldersList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DELETE:
                        if (manager.hasBackups()) return;
                        List<File> clientFolders = manager.getClientFolders();
                        for (File clientFolder : getSelectedClientFolders()) {
                            clientFolders.remove(clientFolder);
                        }
                        updateClientFoldersList();
                        break;
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (backgroundTask != null) {
                    backgroundTask.cancel(true);
                    backgroundTask = null;
                    cancelButton.setEnabled(false);
                }
            }
        });

        chooseDestination.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean chosen = (backupsFolderChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION);
                if (chosen) {
                    final File backupsFolder = backupsFolderChooser.getSelectedFile();
                    backupsFolderField.setText(backupsFolder.getAbsolutePath());
                    backgroundTask = Application.execute(new Runnable() {
                        public void run() {
                            disableCriticalActionButtons(false);
                            startProgressBar();
                            manager.setBackupsFolder(backupsFolder);
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
                boolean chosen = (clientFoldersChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION);
                if (chosen) {
                    for (File clientFolder : clientFoldersChooser.getSelectedFiles()) {
                        try {
                            manager.addClientFolder(clientFolder);
                        } catch (BackupsManager.ClientFoldersListFullException err) {
                            JOptionPane.showMessageDialog(frame, "Limite de 32 pastas, para remover pastas, selecione" +
                                    "a mesma e pressione <delete>.");
                            break;
                        }
                    }
                    updateClientFoldersList();
                }
            }
        });

        makeBackupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!manager.hasEnoughSpaceForBackup()) {
                    JOptionPane.showMessageDialog(frame, "Não há espaço suficiente no destino para realizar o backup.");
                }
                backgroundTask = Application.execute(new Runnable() {
                    public void run() {
                        disableCriticalActionButtons(true);
                        startProgressBar();
                        Transaction t = transactions.makeBackup();
                        try {
                            t.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
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
                backgroundTask = Application.execute(new Runnable() {
                    public void run() {
                        disableCriticalActionButtons(true);
                        startProgressBar();
                        String id = historyModel.getID(row);
                        List<File> clientFolders = getSelectedClientFolders();
                        Transaction t = transactions.restore(id, clientFolders);
                        try {
                            t.execute();
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
