package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.tree.Node;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IncrementalBackup extends Backup {

    public IncrementalBackup(Backup previous) throws IOException {
        super(previous, previous.backupsFolder);
        trees = new HashMap<>();
        Map<File, Node> snapshots = previous.getFullSnapshots();
        for (File folder : snapshots.keySet()) {
            Node tree = snapshots.get(folder).clone();
            trees.put(folder, tree.track(folder, backupFolder));
        }
        presistFile();
    }

    protected IncrementalBackup(String id, Backup previous, Map<File, Node> trees, File backupFolder) {
        super(id, previous, trees, backupFolder);
    }

    //TODO: Watch out for full snapshots only on base backup
    public Map<File, Node> getSnapshots(Collection<File> clientFolders) {
        snapshots = new HashMap<>();
        Map<File, Node> previousSnapshots = previous.getSnapshots(clientFolders);
        for (File folder : previousSnapshots.keySet()) {
            Node tree = previousSnapshots.get(folder).clone();
            tree.merge(trees.get(folder));
            snapshots.put(folder, tree);
        }
        return snapshots;
    }

    public Map<File, Node> getFullSnapshots() {
        // Have to get the base first in order to request for its clientFolders
        // otherwise if I used this clientFolders I could miss one of the originals
        Backup base = getBase();
        return getSnapshots(base.getClientFolders());
    }

    private Backup getBase() {
        Backup backup = this;
        while (backup.previous != null) { backup = backup.previous; }
        return backup;
    }
}
