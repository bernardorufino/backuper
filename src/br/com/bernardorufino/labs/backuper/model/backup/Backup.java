package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.model.tree.FileNode;
import br.com.bernardorufino.labs.backuper.model.tree.Node;
import br.com.bernardorufino.labs.backuper.utils.Utils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static br.com.bernardorufino.labs.backuper.model.tree.Node.*;


public abstract class Backup {

    protected final String id;
    protected Backup previous;
    protected Node modificationsTree;
    protected File backupFolder;
    protected File clientFolder;
    protected File backupsFolder;
    protected Map<Node.Status, Integer> summary;
    protected Node snapshot;

    protected Backup(Backup previous, File clientFolder, File backupsFolder) throws IOException {
        this.previous = previous;
        this.backupsFolder = backupsFolder;
        this.id = generateID();
        this.backupFolder = Utils.createFolder(backupsFolder, id);
        this.clientFolder = clientFolder;
    }

    /* package private */ Backup(String id, Backup previous, Node modificationsTree, File backupFolder,
                                 File clientFolder, File backupsFolder) {
        this.id = id;
        this.previous = previous;
        this.modificationsTree = modificationsTree;
        this.backupFolder = backupFolder;
        this.clientFolder = clientFolder;
        this.backupsFolder = backupsFolder;
    }

    protected String toBuildingString() {
        StringBuilder string = new StringBuilder();
        if (previous == null) {
            string.append(Definitions.BASE_BACKUP_HEAD);
        } else {
            string.append(previous.id);
        }
        if (modificationsTree != null) {
            string.append("\n").append(modificationsTree.toList());
        }
        return string.toString();
    }

    protected final void writeModificationsFile() throws IOException {
        String content = toBuildingString();
        String name = id + "." + Definitions.MODIFICATIONS_FILE_EXTENSION;
        Utils.createContentFile(backupsFolder, name, content);
    }

    private String generateID() {
        return Definitions.DATE_FORMAT.print(DateTime.now());
    }

    public DateTime getDate() {
        return Definitions.DATE_FORMAT.parseDateTime(id);
    }

    public Map<Node.Status, Integer> getSummary() {
        if (summary != null) return summary;
        summary = new HashMap<>();
        for (Status status : Status.values()) {
            summary.put(status, 0);
        }
        if (modificationsTree != null) {
            modificationsTree.traverse(summary, new Node.SimpleTreeWalker<Map<Node.Status, Integer>>() {
                public Map<Node.Status, Integer> visitFile(Map<Node.Status, Integer> memo, FileNode file) {
                    Status status = file.getStatus();
                    summary.put(status, summary.get(status) + 1);
                    return summary;
                }
            });
        }
        return summary;
    }

    public abstract Node getSnapshot();

    public void restore() throws IOException {
        Node snapshot = getSnapshot();
        Utils.purge(clientFolder);
        snapshot.restore(clientFolder);
    }

}
