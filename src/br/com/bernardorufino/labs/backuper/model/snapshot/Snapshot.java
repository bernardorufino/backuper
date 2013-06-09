package br.com.bernardorufino.labs.backuper.model.snapshot;

import java.io.File;

public class Snapshot implements Cloneable {

    public static final String SEPARATOR = File.separator;

    private Node tree;
    private File current;

    public static class LocationDoesNotContainValidFolderException extends RuntimeException { /* Empty */ }
    public static class IncompatibleSnapshotsException extends RuntimeException { /* Empty */ }

    public Snapshot(String currentLocation, String backupLocation) {
        this.current = new File(currentLocation);
        if (!current.isDirectory())
            throw new LocationDoesNotContainValidFolderException();
        File backup = new File(backupLocation);
        if (!backup.isDirectory()) {
            throw new LocationDoesNotContainValidFolderException();
        }
        // Make tree
    }

    // Caution, it's only advisable to call update on fully merged snapshots
    public void update() {
        // Implement =)
    }

    public void merge(Snapshot snapshot) {
        if (current.equals(snapshot.current)) throw new IncompatibleSnapshotsException();
        tree.merge(snapshot.tree);
    }

    private String getCurrentPath(Node node) {
        // relativePath already starts with SEPARATOR
        return current.getPath() + node.getRelativePath();
    }

    private String getBackupPath(Node node) {
        return node.getBackupPath();
    }

    public Snapshot clone() {
        try {
            Snapshot clone = (Snapshot) super.clone();
            clone.tree = tree.clone();
            // Don't need to clone current and backup, File is immutable
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


}
