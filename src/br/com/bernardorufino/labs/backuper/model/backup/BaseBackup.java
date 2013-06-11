package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.snapshot.FolderNode;
import br.com.bernardorufino.labs.backuper.model.snapshot.Node;

import java.io.File;
import java.io.IOException;

public class BaseBackup extends Backup {
    protected Backup previous = null;

    protected BaseBackup(File clientFolder, File backupsFolder) throws IOException {
        super(null, clientFolder, backupsFolder);
        this.modificationsTree = FolderNode.createFromFileSystem(clientFolder, backupFolder);
        writeModificationsFile();
    }

    public Node getSnapshot() {
        return modificationsTree;
    }

}
