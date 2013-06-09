package br.com.bernardorufino.labs.backuper.model.snapshot;

import br.com.bernardorufino.labs.backuper.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public final class FileNode extends Node implements Cloneable {

    /* package private */ FileNode(String name, Status status, DateTime date, File location) {
        super(name, status, date, location);
    }

    public String toList(int level) {
        String indentation = StringUtils.repeat(NodeParser.INDENTATION, level);
        return indentation + toString();
    }

    public Type getType() {
        return Type.File;
    }

    public FileNode clone() {
        return (FileNode) super.clone();
    }

    public FileNode update(File file) throws IOException {
        if (!file.isFile()) throw new IncompatibleNodeUpdateException();
        //TODO: Check if files are parallel, maybe if relativePath is to the right of file.getAbsolutePath().
        DateTime fileDate = Utils.getDate(file);
        // If !(date < fileDate) = date >= fileDate, then no need to update
        if (!date.isBefore(fileDate)) return null;
        makeBackup(file);
        return new FileNode(name, Status.Modify, fileDate, location); // It's orphan
    }

    private void makeBackup(File file) throws IOException {
        Utils.copy(file, getBackupPath());
    }

    public void merge(Node node) {
        if (!parallel(node)) throw new IncompatibleNodeMergeException();
        FileNode recent = (FileNode) node;
        switch (status) {
            case Create:
                switch (recent.status) {
                    case Create: throw new IncompatibleNodeMergeException();
                    case Delete: destroy(); break;
                    case Modify: safeUpdate(Status.Create, recent); break;
                    case Existent: safeUpdate(Status.Create, recent); break;
                }
                break;
            case Delete:
                switch (recent.status) {
                    case Create: safeUpdate(Status.Create, recent); break;
                    case Delete:
                    case Modify:
                    case Existent: throw new IncompatibleNodeMergeException();
                }
                break;
            case Modify:
                switch (recent.status) {
                    case Create: throw new IncompatibleNodeMergeException();
                    case Delete: safeUpdate(Status.Delete, recent); break;
                    case Modify: safeUpdate(Status.Modify, recent); break;
                    case Existent: safeUpdate(Status.Create, this); break;
                }
                break;
            case Existent:
                switch (recent.status) {
                    case Create: throw new IncompatibleNodeMergeException();
                    case Delete: safeUpdate(Status.Delete, recent); break;
                    case Modify: safeUpdate(Status.Modify, recent); break;
                    case Existent: safeUpdate(Status.Existent, recent /* or this */); break;
                }
                break;
        }
    }

    private void destroy() {
        parent.removeChild(name);
    }

    private void safeUpdate(Status status, Node update) {
        this.status = status;
        this.location = update.location;
        this.date = update.date;
    }

}
