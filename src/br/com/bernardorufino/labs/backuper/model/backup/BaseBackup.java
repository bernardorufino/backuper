package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.tree.FolderNode;
import br.com.bernardorufino.labs.backuper.model.tree.Node;

import java.io.File;
import java.io.IOException;

public class BaseBackup extends Backup {
    protected Backup previous = null;

    protected BaseBackup(File clientFolder, File backupsFolder) throws IOException {
        super(null, clientFolder, backupsFolder);
        this.modificationsTree = Node.createFromFileSystem(clientFolder, backupFolder);
        writeModificationsFile();
    }

    protected BaseBackup(String id, Node modificationsTree, File backupFolder, File clientFolder, File backupsFolder) {
        super(id, null, modificationsTree, backupFolder, clientFolder, backupsFolder);
    }

    public Node getSnapshot() {
        return modificationsTree;
    }

}
