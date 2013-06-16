package br.com.bernardorufino.labs.backuper.model.tree;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.utils.Utils;
import static br.com.bernardorufino.labs.backuper.utils.Utils.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class FolderNode extends Node implements Cloneable {

    public static FolderNode getDummyRoot() {
        FolderNode dummy = new FolderNode(null, Status.Create, DateTime.now(), null);
        dummy.relativePath = "";
        return dummy;
    }

    // Using Map for O(n) merge instead of O(n^2) in merge()
    // LinkedHashSet for preserving order in children, although children
    // don't need to be ordered in a folder, using for fast equality checking
    private Map<String, Node> children = new LinkedHashMap<>();

    /* package private */
    public FolderNode(String name, Status status, DateTime date, File location) {
        super(name, status, date, location);
    }

    // In order to create from the file system we need parent before-hand
    public FolderNode(File folder, File location, FolderNode parent) throws IOException {
        super(folder.getName(), Status.Create, Utils.getDate(folder), location);
        if (!isParallel(folder)) throw new IncompatibleNodeException();
        if (parent != null) setParent(parent);
        Utils.createFolder(getBackupPath());
        //noinspection ConstantConditions IntelliJ
        for (File fsNode : folder.listFiles()) {
            Node child = Node.createFromFileSystem(fsNode, location, this);
            addChild(child);
        }
    }

    public <T> T traverse(T memo, TreeWalker<T> walker) {
        memo = walker.preChildren(memo, this);
        for (Node node : children.values()) {
            memo = node.traverse(memo, walker);
        }
        memo = walker.postChildren(memo, this);
        return memo;
    }

    // To be called only when this folder is already attached to it's parent
    // and when it's already filled up with it's children
    // will recursively call it's children backup() method as well
    public void backup(File folder) throws IOException {
        Utils.createFolder(getBackupPath());

    }

    /* package private */
    public void addChild(Node child) {
        children.put(child.name, child);
        if (child.date.isAfter(date)) date = child.date;
        child.setParent(this);
    }

    public Map<String, Node> getChildrenMap() {
        return children;
    }

    public List<Node> getChildren() {
        return new ArrayList<>(children.values());
    }

    public void removeChild(String name){
        children.remove(name);
    }

    protected void replaceChild(Node child) {
        children.put(child.name, child);
    }

    public String toList(int level) {
        StringBuilder list = new StringBuilder();
        list.append(StringUtils.repeat(Definitions.INDENTATION, level));
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
        if (node == null) return;
        if (!parallel(node)) throw new IncompatibleNodeMergeException();
        FolderNode recentFolder = (FolderNode) node;
        Map<String, Node> recents = new HashMap<>(recentFolder.children);
        // In order to preserve the loop, because inside the first for,
        // child nodes of this node can be removed by FileNode.merge,
        // and it can avoid the loop
        List<Node> childrenNodes = new ArrayList<>(children.values());
        for (Node old : childrenNodes) {
            // If recent is nonexistent preserve old one
            if (!recents.containsKey(old.name)) continue;
//            System.out.println("Merging " + old + " with " + recents.get(old.name));
            Node n = recents.get(old.name);
//            System.out.println(old.name);
            old.merge(n);
            recents.remove(old.name);
        }
        for (Node recent : recents.values()) {
            // If only recent exist, simple add a clone of it to children
            addChild(recent.clone());
        }
        // Now updates its metadata (status, date and location)
        super.merge(node);
    }

    protected void destroy() {
        parent.removeChild(name);
    }

    public FolderNode track(File folder, File newLocation) throws IOException {
//        System.out.println("track(" + folder.getName() + ")");
        if (!isParallel(folder)) throw new IncompatibleNodeException();
        FolderNode newFolderNode = getModifiedScaffold(newLocation);
        Map<String, Node> currentChildren = new LinkedHashMap<>(children);
        //noinspection ConstantConditions IntelliJ
        for (File fsNode : folder.listFiles()) {
            Node currentChild = currentChildren.remove(fsNode.getName());
            if (currentChild == null) { // New fsNode
                Node newChildNode = Node.createFromFileSystem(fsNode, newLocation, newFolderNode);
                newFolderNode.addChild(newChildNode);
            } else if (Utils.isAfter(getDate(fsNode), currentChild.date) || currentChild.isFolder()) { // Modified fsNode or folder
                Node modifiedChildNode = currentChild.track(fsNode, newLocation);
                if (modifiedChildNode != null) {
                    newFolderNode.addChild(modifiedChildNode);
                }
            } // else don't add in newFolderNode because it's already here and up to date,
              // thus don't need to be in the next (incremental) folder
        }
        // Remaining nodes in current tree, if they are still in currentChildren, it means they
        // aren't in the file system, thus need to be marked for deletion
        for (Node node : currentChildren.values()) {
            Node deletedNode = node.markForDeletion();
            newFolderNode.addChild(deletedNode);
        }
        if (newFolderNode.children.isEmpty()) newFolderNode = null;
        return newFolderNode;
    }

    // Check wheter cloning it's not best
    public FolderNode getModifiedScaffold(File newLocation) {
        FolderNode node = new FolderNode(name, Status.Modify, date, newLocation);
        node.setParent(parent);
        node.relativePath = relativePath;
        return node;
    }

    public FolderNode clone() {
        FolderNode clone = (FolderNode) super.clone();
        clone.children = new LinkedHashMap<>();
        for (Node node : children.values()) {
            clone.addChild(node.clone());
        }
        return clone;
    }

    public void restore(File clientLocation) throws IOException {
        // Have to copy the empty folder first
        Utils.createFolder(getFullPath(clientLocation.getParentFile()));
        for (Node node : children.values()) {
            node.restore(clientLocation);
        }
    }

    protected FolderNode markForDeletion() {
        FolderNode clone = clone();
        clone.status = Status.Delete;
        for (Node child : clone.getChildren()) {
            clone.replaceChild(child.markForDeletion());
        }
        return clone;
    }
}
