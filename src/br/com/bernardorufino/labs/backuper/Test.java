package br.com.bernardorufino.labs.backuper;

import br.com.bernardorufino.labs.backuper.model.backup.BackupsManager;
import br.com.bernardorufino.labs.backuper.model.tree.FileNode;
import br.com.bernardorufino.labs.backuper.model.tree.FolderNode;
import br.com.bernardorufino.labs.backuper.model.tree.Node;
import br.com.bernardorufino.labs.backuper.utils.Utils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        testBackup();
    }

    public static void testBackup() throws IOException {
        BackupsManager manager = new BackupsManager(new File("D:\\Backuper\\Backup"), new File("D:\\Backuper\\Client"));
        manager.fetchBackups();
        manager.makeBackup();
    }

    public static void testTree() {
        FolderNode root = new FolderNode("Main", Node.Status.Existent, DateTime.now(), null);
        for (String name : new String[] { "FileName.java", "FolderNode.java", "Node.java" }) {
            root.addChild(new FileNode(name, Node.Status.Existent, DateTime.now(), null));
        }
        FolderNode p = new FolderNode("Subfolder", Node.Status.Existent, DateTime.now(), null);
        root.addChild(p);
        for (String name : new String[] { "BackupsView.java", "Snapshot.java" }) {
            p.addChild(new FileNode(name, Node.Status.Existent, DateTime.now(), null));
        }
        FolderNode q = new FolderNode("Subsubfolder", Node.Status.Existent, DateTime.now(), null);
        p.addChild(q);
        for (String name : new String[] { "Initializer.java", "Gleup.class" }) {
            q.addChild(new FileNode(name, Node.Status.Existent, DateTime.now(), null));
        }
        root.addChild(new FolderNode("EmptyFolder", Node.Status.Create, DateTime.now(), null));
        root.addChild(new FileNode("FooFile.kiu", Node.Status.Create, DateTime.now(), null));

        String list = root.toList();
        System.out.println(list);
        root = (FolderNode) Node.fromList(list, null);
        String other = root.toList();
        System.out.println(other);
        System.out.println(list.equals(other));
    }

}
