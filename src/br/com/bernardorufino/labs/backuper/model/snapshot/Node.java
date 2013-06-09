package br.com.bernardorufino.labs.backuper.model.snapshot;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public abstract class Node {

    public static class IncompatibleNodeMergeException extends RuntimeException { /* Empty */ }
    public static class IncompatibleNodeUpdateException extends RuntimeException { /* Empty */ }


    public static enum Type { File, Folder;}
    public static enum Status { Create, Delete, Modify, Existent;}

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
        this.relativePath = "";
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
        relativePath = parent.relativePath + Snapshot.SEPARATOR + name; // Assuming top down creation...
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getBackupPath() {
        // relativePath already starts with SEPARATOR
        return location.getPath() + getRelativePath();
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

    // Caution, it's only advisable to call update on fully merged trees
    public abstract Node update(File fileSystemNode) throws IOException;

    public boolean parallel(Node node) {
        //TODO: Remove line below, just for Debugging purpose
        if (node == null) throw new RuntimeException("Possible mistake, checking parallel with null");
        return node != null && getType() == node.getType() && relativePath.equals(node.relativePath);
    }

    protected Node clone() {
        // Not throwing exception because it's known beforehand that
        // all subclasses implement Cloneable
        try {
            // All fields are immutable, don't need cloning them
            return (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
