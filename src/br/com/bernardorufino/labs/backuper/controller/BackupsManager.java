package br.com.bernardorufino.labs.backuper.controller;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.model.backup.Backup;
import br.com.bernardorufino.labs.backuper.model.backup.BackupParser;
import br.com.bernardorufino.labs.backuper.model.backup.BaseBackup;
import br.com.bernardorufino.labs.backuper.model.backup.IncrementalBackup;
import br.com.bernardorufino.labs.backuper.model.tree.Node;
import br.com.bernardorufino.labs.backuper.libs.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackupsManager {

    private File backupsFolder;
    private List<File> clientFolders;
    private NavigableMap<String, Backup> history;

    public BackupsManager() {
        history = new TreeMap<>();
        clientFolders = new ArrayList<>();
    }

    public void fetchBackups() throws IOException {
        history.clear();
        for (File file : backupsFolder.listFiles()) {
            if (!BackupParser.isModificationsFile(file)) continue;
            BackupParser.buildInHistory(file, history);
        }
        if (history.size() > 0) {
            Backup base = history.firstEntry().getValue();
            clientFolders = base.getClientFolders();
        }
    }

    public List<Backup> getBackups() {
        return new ArrayList<>(history.values());
    }

    public Backup getRecent() {
        return history.lastEntry().getValue();
    }

    public boolean hasBackups() {
        return history.size() > 0;
    }

    public List<File> getClientFolders() {
        return clientFolders;
    }

    public void setBackupsFolder(File backupsFolder) {
        history.clear();
        clientFolders.clear();
        this.backupsFolder = backupsFolder;
    }

    public void addClientFolder(File clientFolder) {
        clientFolders.add(clientFolder);
    }

    public void setClientFolders(List<File> clientFolders) {
        this.clientFolders = clientFolders;
    }

    private Backup add(Backup backup) {
        history.put(backup.id, backup);
        return backup;
    }

    public Backup makeBackup() throws IOException {
        if (history.size() > 0) {
            return add(new IncrementalBackup(getRecent()));
        } else {
            return add(new BaseBackup(clientFolders, backupsFolder));
        }
    }

    public void restore(String id, Collection<File> clientFolders) throws IOException {
        history.get(id).restore(clientFolders);
        makeBackup();
    }

}
