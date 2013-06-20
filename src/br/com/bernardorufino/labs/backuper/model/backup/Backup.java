package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.tree.FileNode;
import br.com.bernardorufino.labs.backuper.model.tree.Node;
import br.com.bernardorufino.labs.backuper.libs.Utils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static br.com.bernardorufino.labs.backuper.config.Definitions.*;
import static br.com.bernardorufino.labs.backuper.model.tree.Node.*;

public abstract class Backup {

    public final String id;
    protected Backup previous;
    protected File backupFolder;
    protected Map<File, Node> trees;
    protected File backupsFolder;
    protected Map<Node.Status, Integer> summary;
    protected Map<File, Node> snapshots;

    protected Backup(Backup previous, File backupsFolder) throws IOException {
        this.previous = previous;
        this.backupsFolder = backupsFolder;
        this.id = generateID();
        this.backupFolder = Utils.createFolder(backupsFolder, id);
        this.trees = new LinkedHashMap<>();
    }

    /* package private */ Backup(String id, Backup previous, Map<File, Node> trees, File backupFolder) {
        this.id = id;
        this.previous = previous;
        this.trees = trees;
        this.backupFolder = backupFolder;
        this.backupsFolder = backupFolder.getParentFile();
    }

    protected String toBuildingString() {
        StringBuilder string = new StringBuilder();
        string.append((previous == null) ? BASE_BACKUP_HEAD : previous.id);
        string.append("\n");
        for (File file : trees.keySet()) {
            Node tree = trees.get(file);
            if (tree == null) continue;
            string.append(CLIENT_FOLDER_MARKER);
            string.append(DELIMITER);
            string.append(file.getAbsolutePath());
            string.append("\n");
            string.append(tree.toList());
            string.append("\n");
        }
        return string.toString();
    }

    protected final void presistFile() throws IOException {
        String content = toBuildingString();
        String name = id + "." + MODIFICATIONS_FILE_EXTENSION;
        Utils.createContentFile(backupsFolder, name, content);
    }

    private String generateID() {
        return DATE_FORMAT.print(DateTime.now());
    }

    public DateTime getDate() {
        return DATE_FORMAT.parseDateTime(id);
    }

    public List<File> getClientFolders() {
        return new ArrayList<>(trees.keySet());
    }

    public Map<Node.Status, Integer> getSummary() {
        if (summary != null) return summary;
        summary = new HashMap<>();
        for (Status status : Status.values()) {
            summary.put(status, 0);
        }
        for (Node tree : trees.values()) {
            if (tree == null) continue;
            tree.traverse(summary, new Node.SimpleTreeWalker<Map<Node.Status, Integer>>() {
                public Map<Node.Status, Integer> visitFile(Map<Node.Status, Integer> memo, FileNode file) {
                    Status status = file.getStatus();
                    summary.put(status, summary.get(status) + 1);
                    return summary;
                }
            });
        }
        return summary;
    }

    public abstract Map<File, Node> getSnapshots(Collection<File> clientfolders);

    public abstract Map<File, Node> getFullSnapshots();

    public void restore(Collection<File> clientFolders) throws IOException {
        Map<File, Node> snapshots = getSnapshots(clientFolders);
        for (File folder : snapshots.keySet()) {
            Utils.purge(folder);
            snapshots.get(folder).restore(folder);
        }
    }

    public String getID() {
        return id;
    }

}