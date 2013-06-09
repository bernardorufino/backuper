package br.com.bernardorufino.labs.backuper.model.snapshot;

import br.com.bernardorufino.labs.backuper.utils.Utils;
import static br.com.bernardorufino.labs.backuper.utils.Utils.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class FolderNode extends Node implements Cloneable {

    public static FolderNode getDummy() {
        return new FolderNode("dummy", Status.Existent, DateTime.now(), null);
    }

    // Using Map for O(n) merge instead of O(n^2) in merge()
    // LinkedHashSet for preserving order in children, although children
    // don't need to be ordered in a folder, using for fast equality checking
    private Map<String, Node> children = new LinkedHashMap<>();

    /* package private */ FolderNode(String name, Status status, DateTime date, File location) {
        super(name, status, date, location);
    }

    /* package private */ void addChild(Node child) {
        children.put(child.name, child);
        child.setParent(this);
    }

    public Map<String, Node> getChildren() {
        return children;
    }

    public void removeChild(String name){
        children.remove(name);
    }

    public String toList(int level) {
        StringBuilder list = new StringBuilder();
        list.append(StringUtils.repeat(NodeParser.INDENTATION, level));
        list.append(toString());
        for (Node p : children.values()) {
            list.append("\n").append(p.toList(level + 1));
        }
        return list.toString();
    }

    public Type getType() {
        return Type.Folder;
    }

    public void merge(Node node) {
        if (!parallel(node)) throw new IncompatibleNodeMergeException();
        FolderNode recentFolder = (FolderNode) node;
        Map<String, Node> recents = new HashMap<>(recentFolder.getChildren());
        for (Node old : children.values()) {
            // If recent is nonexistent preserve old one
            if (!recents.containsKey(old.name)) continue;
            old.merge(recents.get(old.name));
            recents.remove(old.name);
        }
        for (Node recent : recents.values()) {
            if (recent.status == Status.Existent)
                throw new IncompatibleNodeMergeException();
            // If only recent exist, simple add a clone of it to children
            addChild(recent.clone());
        }
    }

    public FolderNode update(File folder) throws IOException {
        if (!folder.isDirectory()) throw new IncompatibleNodeUpdateException();
        FolderNode newFolderNode = new FolderNode(name, Status.Modify, date, location);
        Map<String, Node> olds = new LinkedHashMap<>(children);

        for (File fsNode : folder.listFiles()) {
            Node old = olds.get(fsNode.getName());
            if (old == null) { // New file OR NODE,
                //TODO: FIX! Create new folders, here only support files
                FileNode newFileNode = new FileNode(fsNode.getName(), Status.Modify, getDate(fsNode), location);
                newFolderNode.addChild(newFileNode);
                // continue; ?
            } else if (getDate(fsNode).isAfter(old.date)) { // Modified file
                //TODO: CHeck if support modfified Folders
                Node updatedFileNode = old.update(fsNode);
                newFolderNode.addChild(updatedFileNode);
            }
            // Remove old from olds
        }
        // Iterate through olds (which contains the remaining children of current node),
        // and add nodes marked for deletion, (maybe recursive deletion for folders?)

        // Check if there was modifications (maybe create a boolean), if there wasn't simple return null
        // otherwise return newFolderNode;
        return null;
    }

    public FolderNode clone() {
        FolderNode clone = (FolderNode) super.clone();
        clone.children = new LinkedHashMap<>();
        for (Node node : children.values()) {
            addChild(node.clone());
        }
        return clone;
    }

}
