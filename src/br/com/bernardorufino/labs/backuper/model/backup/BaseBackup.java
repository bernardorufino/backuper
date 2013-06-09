package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.snapshot.Snapshot;

public class BaseBackup extends Backup {
    protected Backup previous = null;


    public Backup getPrevious() {
        return null;
    }
}
