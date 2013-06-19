package br.com.bernardorufino.labs.backuper.controller;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.libs.Utils;
import br.com.bernardorufino.labs.backuper.model.backup.Backup;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static br.com.bernardorufino.labs.backuper.config.Definitions.*;

public class BackupsTransaction {
    private final BackupsManager manager;


    public BackupsTransaction(BackupsManager manager) {
        this.manager = manager;
    }

    public Transaction makeBackup() {
        return new Transaction() {
            private Backup lastBackup;

            public void commit() throws IOException {
                lastBackup = manager.getRecent();
                manager.makeBackup();
            }

            public void rollback(Throwable e) throws IOException {
                Backup recent = manager.getRecent();
                if (lastBackup != recent) {
                    manager.delete(recent.id);
                }
                manager.flush();
            }

        };
    }

    public Transaction restore(final String id, final List<File> clientFolders) {
        return new Transaction() {
            private String safetyBackupID;

            public void commit() throws IOException {
                manager.makeBackup();
                safetyBackupID = manager.getRecent().id;
                manager.restore(id, clientFolders);
            }

            public void rollback(Throwable e) throws Throwable {
                if (safetyBackupID != null) {
                    manager.restore(safetyBackupID, clientFolders);
                }
            }

        };
    }
}
