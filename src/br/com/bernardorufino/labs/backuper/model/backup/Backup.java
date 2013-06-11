package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.model.snapshot.FolderNode;
import br.com.bernardorufino.labs.backuper.model.snapshot.Node;
import br.com.bernardorufino.labs.backuper.utils.Utils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Backup {

    private static final DateTimeFormatter DATE_TIME_ID_FORMAT = DateTimeFormat.forPattern("ddMMyyyyHHmmssS");

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
        Path newLocation = backupsFolder.toPath().resolve(id);
        this.backupFolder = Files.createDirectory(newLocation).toFile();
        this.clientFolder = clientFolder;
    }

    protected final void writeModificationsFile() throws IOException {
        if (modificationsTree == null) throw new IllegalStateException();
        String content = modificationsTree.toList();
        Utils.createContentFile(backupsFolder, id, content);
    }

    private String generateID() {
        return DATE_TIME_ID_FORMAT.print(DateTime.now());
    }

    public abstract Node getSnapshot();

    public Backup updateBackup() throws IOException {
        return new IncrementalBackup(this);
    }

    public void restore() throws IOException {
        Node snapshot = getSnapshot();
        Utils.purge(clientFolder);
        snapshot.restore(clientFolder);
    }

}
