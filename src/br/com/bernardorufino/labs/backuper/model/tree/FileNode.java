package br.com.bernardorufino.labs.backuper.model.tree;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public final class FileNode extends Node implements Cloneable {

    /* package private */
    public FileNode(String name, Status status, DateTime date, File location) {
        super(name, status, date, location);
    }

    public FileNode(File file, File location, FolderNode parent) throws IOException {
        super(file.getName(), Status.Create, Utils.getDate(file), location);
        if (!isParallel(file)) throw new IncompatibleNodeException();
        if (parent != null) setParent(parent);
        Utils.copyIntoFolder(file, parent.getBackupPath());
    }

    public <T> T traverse(T memo, TreeWalker<T> walker) {
        return walker.visitFile(memo, this);
    }

    public String toList(int level) {
        String indentation = StringUtils.repeat(Definitions.INDENTATION, level);
        return indentation + toString();
    }

    public Type getType() {
        return Type.File;
    }

    public FileNode clone() {
        return (FileNode) super.clone();
    }

    public void restore(File clientLocation) throws IOException {
        Utils.copy(getBackupFsNode(), getFullPath(clientLocation.getParentFile()));
    }

    public FileNode track(File file, File newLocation) throws IOException {
        if (!isParallel(file)) throw new IncompatibleNodeException();
        DateTime fileDate = Utils.getDate(file);
        // If !(date < fileDate) = date >= fileDate, then no need to update
        if (!date.isBefore(fileDate)) return null;
        Utils.copy(file, getFullPath(newLocation));
        // Returns orphan FileNode, needs to make it child of some FolderNode
        return new FileNode(name, Status.Modify, fileDate, location);
    }

    protected void destroy() {
        parent.removeChild(name);
    }

    protected FileNode markForDeletion() {
        FileNode clone = clone();
        clone.status = Status.Delete;
        return clone;
    }
}
