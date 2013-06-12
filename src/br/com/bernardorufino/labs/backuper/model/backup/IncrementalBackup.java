package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.tree.Node;

import java.io.IOException;

public class IncrementalBackup extends Backup {

    protected IncrementalBackup(Backup previous) throws IOException {
        super(previous, previous.clientFolder, previous.backupsFolder);
        this.modificationsTree = previous.modificationsTree.update(clientFolder);
        writeModificationsFile();
    }

    public Node getSnapshot() {
        Node snapshot = previous.getSnapshot().clone();
        snapshot.merge(modificationsTree);
        return snapshot;
    }

}
