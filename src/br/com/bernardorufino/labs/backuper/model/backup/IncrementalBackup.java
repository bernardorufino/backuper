package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.tree.Node;

import java.io.File;
import java.io.IOException;

public class IncrementalBackup extends Backup {

    protected IncrementalBackup(Backup previous) throws IOException {
        super(previous, previous.clientFolder, previous.backupsFolder);
        this.modificationsTree = previous.modificationsTree.track(clientFolder);
        writeModificationsFile();
    }

    protected IncrementalBackup(String id, Backup previous, Node modificationsTree, File backupFolder,
                                File clientFolder, File backupsFolder) {
        super(id, previous, modificationsTree, backupFolder, clientFolder, backupsFolder);
    }

    public Node getSnapshot() {
        Node snapshot = previous.getSnapshot().clone();
        snapshot.merge(modificationsTree);
        return snapshot;
    }

}
