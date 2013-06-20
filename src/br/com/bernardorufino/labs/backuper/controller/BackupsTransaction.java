package br.com.bernardorufino.labs.backuper.controller;

import br.com.bernardorufino.labs.backuper.Application;
import br.com.bernardorufino.labs.backuper.libs.Utils;
import br.com.bernardorufino.labs.backuper.model.backup.Backup;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
            private Backup safetyBackup;

            public void commit() throws IOException {

                Utils.uninterruptable();
                manager.makeBackup();
                safetyBackup = manager.getRecent();
                Utils.interruptable();

                manager.restore(id, clientFolders);

                Utils.uninterruptable();
                manager.delete(safetyBackup.id);
                manager.makeBackup();
                Utils.interruptable();

            }

            public void rollback(Throwable e) throws Throwable {
                // Check if the cause for stopping is the restore() and not the getRecent()
                if (safetyBackup != null) {

                    Utils.uninterruptable();
                    manager.restore(safetyBackup.id, clientFolders);
                    manager.delete(safetyBackup.id);
                    Utils.interruptable();

                }
            }

        };
    }
}
