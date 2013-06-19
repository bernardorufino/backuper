package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.libs.Utils;
import br.com.bernardorufino.labs.backuper.model.tree.Node;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BaseBackup extends Backup {
    protected Backup previous = null;

    public BaseBackup(List<File> clientFolders, File backupsFolder) throws IOException {
        super(null, backupsFolder);
        for (File folder : clientFolders) {
            trees.put(folder, Node.fromFileSystem(folder, backupFolder));
        }
        presistFile();
    }

    public BaseBackup(String id, Map<File, Node> trees, File backupFolder) {
        super(id, null, trees, backupFolder);
    }

    public Map<File, Node> getSnapshots(Collection<File> clientFolders) {
        return Utils.extract(trees, clientFolders);
    }

    public Map<File, Node> getFullSnapshots() {
        return getSnapshots(getClientFolders());
    }
}
