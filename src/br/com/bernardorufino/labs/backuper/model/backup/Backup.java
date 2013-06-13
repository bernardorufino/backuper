package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.tree.Node;
import br.com.bernardorufino.labs.backuper.utils.Utils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Backup {

    protected final String id;
    protected Backup previous;
    protected Node modificationsTree;
    protected File backupFolder;
    protected File clientFolder;
    protected File backupsFolder;

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
        if (modificationsTree == null) throw new IllegalStateException();
        StringBuilder string = new StringBuilder();
        if (previous == null) {
            string.append(BackupsManager.BASE_BACKUP_HEAD).append("\n");
        } else {
            string.append(id).append("\n");
        }
        string.append(modificationsTree.toList());
        return string.toString();
    }

    protected final void writeModificationsFile() throws IOException {
        String content = toBuildingString();
        String name = id + "." + BackupsManager.MODIFICATIONS_FILE_EXTENSION;
        Utils.createContentFile(backupsFolder, name, content);
    }

    private String generateID() {
        return BackupsManager.DATE_FORMAT.print(DateTime.now());
    }

    public abstract Node getSnapshot();

    public void restore() throws IOException {
        Node snapshot = getSnapshot();
        Utils.purge(clientFolder);
        snapshot.restore(clientFolder);
    }

}
