package br.com.bernardorufino.labs.backuper.controller;

import br.com.bernardorufino.labs.backuper.model.backup.Backup;
import br.com.bernardorufino.labs.backuper.model.backup.BackupsManager;
import br.com.bernardorufino.labs.backuper.view.HistoryAdapter;

import java.io.File;
import java.io.IOException;

public class BackupsController {
    public BackupsManager manager;

    public void makeBackup() throws IOException {
        manager.makeBackup();
    }

    public void restore(int index) throws IOException {
        Backup backup = manager.getBackups().get(index);
        manager.restore(backup.getID());
    }

    public void setUp(String backupsFolder, String clientFolder) throws IOException {
        manager = new BackupsManager(new File(backupsFolder), new File(clientFolder));
        manager.fetchBackups();
    }

    public HistoryAdapter getHistoryAdapter() {
        return new HistoryAdapter(manager.getBackups());
    }


}
