package br.com.bernardorufino.labs.backuper.model.tree;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import static br.com.bernardorufino.labs.backuper.utils.Utils.*;

public abstract class Node {

    public static final String SEPARATOR = File.separator;

    public static class IncompatibleNodeMergeException extends RuntimeException { /* Empty */ }

    public static class IncompatibleNodeException extends RuntimeException { /* Empty */ }


    public static enum Type { File, Folder }

    public static enum Status { Create, Delete, Modify, Existent }


    public static Node fromList(String list, File location) {
        return NodeParser.fromList(list, location);
    }

    public static Node create(Type type, String name, Status status, DateTime date, File location) {
        switch (type) {
            case File: return new FileNode(name, status, date, location);
            case Folder: return new FolderNode(name, status, date, location);
        }
        return null;
    }

    public static Node createFromFileSystem(File fsNode, File location, FolderNode parent) throws IOException {
        if (fsNode.isFile()) {
            return new FileNode(fsNode, location, parent);
        } else {
            return new FolderNode(fsNode, location, parent);
        }
    }

    public static Node createFromFileSystem(File fsNode, File location) throws IOException {
        return createFromFileSystem(fsNode, location, null);
    }

    protected final String name;

    protected Status status;
    protected DateTime date;
    protected String relativePath;
    // Location is not the path to the file or folder directly, instead is a path
    // to the space where the backup files are, use getBackupPath() for the former
    protected File location;
    protected FolderNode parent;

    protected Node(String name, Status status, DateTime date, File location) {
        this.name = name;
        this.status = status;
        this.date = date;
        this.relativePath = name;
        this.location = location;
    }

    public abstract String toList(int level);

    public String toList() {
        return toList(0);
    }

    public abstract Type getType();

    public boolean isFolder() { return getType() == Type.Folder; }

    public boolean isFile() { return getType() == Type.File; }

    public void setParent(FolderNode parent) {
        this.parent = parent;
        // Assuming top down creation
        relativePath = parent.relativePath + SEPARATOR + name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getBackupPath() {
        return location.getPath() + SEPARATOR + relativePath;
    }

    public File getBackupFsNode() {
        // File is already immutable, don't care about caching the file in the object
        return new File(getBackupPath());
    }

    public String toString() {
        String property;
        StringBuilder string = new StringBuilder();
        property = NodeParser.toProperty(getType().toString());
        string.append(property).append(NodeParser.DELIMITER);
        property = NodeParser.toProperty(status.toString());
        string.append(property).append(NodeParser.DELIMITER);
        property = NodeParser.DATE_FORMATTER.print(date);
        property = NodeParser.toProperty(property);
        string.append(property).append(NodeParser.DELIMITER);
        string.append(name);
        return string.toString();
    }

    public abstract void merge(Node node);

    public boolean isParallel(File file) {
        return getType() == fsNodeType(file)
               && Pattern.compile(Pattern.quote(getRelativePath()) + "$").matcher(file.getAbsolutePath()).find();
    }

    protected Type fsNodeType(File fsNode) {
        return fsNode.isFile() ? Type.File : Type.Folder;
    }

    // Caution, it's only advisable to call update on fully merged trees
    public abstract Node track(File fileSystemNode) throws IOException;

    public boolean parallel(Node node) {
        //TODO: Remove line below, just for Debugging purpose
        if (node == null) throw new RuntimeException("Possible mistake, checking parallel with null");
        return node != null && getType() == node.getType() && relativePath.equals(node.relativePath);
    }

    public Node markForDeletion() {
        Node clone = clone();
        clone.status = Status.Delete;
        return clone;
    }

    // Not throwing exception because it's known beforehand that
    // all subclasses implement Cloneable
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public Node clone() {
        try {
            // All fields are immutable, don't need cloning them
            return (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public abstract void restore(File clientLocation) throws IOException;

}
