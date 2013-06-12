package br.com.bernardorufino.labs.backuper.model.tree;

import br.com.bernardorufino.labs.backuper.utils.Utils;
import static br.com.bernardorufino.labs.backuper.utils.Utils.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class FolderNode extends Node implements Cloneable {

    public static FolderNode getDummy() { return new FolderNode(null, Status.Existent, DateTime.now(), null); }

    public static FolderNode createFromFileSystem(File folder, File location) throws IOException {
        FolderNode node = (FolderNode) Node.create(folder, Status.Create, location);
        node.update(folder);
        return node;
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
        if (child.date.isAfter(date)) date = child.date;
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
        if (!isParallel(folder)) throw new IncompatibleNodeUpdateException();
        FolderNode newFolderNode = getModifiedScaffold();
        Map<String, Node> currentChildren = new LinkedHashMap<>(children);
        //noinspection ConstantConditions IntelliJ
        for (File fsNode : folder.listFiles()) {
            Node currentChild = currentChildren.remove(fsNode.getName());
            if (currentChild == null) { // New fsNode
                Node newChildNode = Node.create(fsNode, Status.Modify, location);
                newFolderNode.addChild(newChildNode);
            } else if (getDate(fsNode).isAfter(currentChild.date)) { // Modified fsNode
                Node modifiedChildNode = currentChild.update(fsNode);
                newFolderNode.addChild(modifiedChildNode);
            } // else don't add in newFolderNode because it's already here and up to date,
              // thus don't need to be in the next (incremental) folder
        }
        // Remaining nodes in current tree, if they are still in currentChildren, it means they
        // aren't in the file system, thus need to be marked for deletion
        for (Node node : currentChildren.values()) {
            Node deletedNode = node.markForDeletion();
            newFolderNode.addChild(deletedNode);
        }
        if (newFolderNode.getChildren().isEmpty()) newFolderNode = null;
        return newFolderNode;
    }

    public FolderNode getModifiedScaffold() {
        return new FolderNode(name, Status.Modify, date, location);
    }

    public FolderNode clone() {
        FolderNode clone = (FolderNode) super.clone();
        clone.children = new LinkedHashMap<>();
        for (Node node : children.values()) {
            addChild(node.clone());
        }
        return clone;
    }

    public void restore(File clientLocation) throws IOException {
        // Have to copy the empty folder first
        Utils.copy(getBackupFsNode(), clientLocation);
        for (Node node : children.values()) {
            node.restore(clientLocation);
        }
    }

}