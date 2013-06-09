package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.snapshot.Snapshot;

public abstract class Backup {
    protected Backup previous;
    protected Snapshot snapshot;

    public static void main(String[] args) {
    }

    public Backup getPrevious() {
        return previous;
    }
}
