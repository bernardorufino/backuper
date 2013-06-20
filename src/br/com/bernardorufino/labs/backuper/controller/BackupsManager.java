package br.com.bernardorufino.labs.backuper.controller;

import br.com.bernardorufino.labs.backuper.libs.Utils;
import br.com.bernardorufino.labs.backuper.model.backup.Backup;
import br.com.bernardorufino.labs.backuper.model.backup.BackupParser;
import br.com.bernardorufino.labs.backuper.model.backup.BaseBackup;
import br.com.bernardorufino.labs.backuper.model.backup.IncrementalBackup;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static br.com.bernardorufino.labs.backuper.config.Definitions.MAX_CLIENT_FOLDERS;
import static br.com.bernardorufino.labs.backuper.config.Definitions.MODIFICATIONS_FILE_EXTENSION;

public class BackupsManager {

    public static class NotEnoughDiskSpaceException extends RuntimeException { /* Empty */ }
    public static class ClientFoldersListFullException extends RuntimeException { /* Empty */ }

    private File backupsFolder;
    private List<File> clientFolders;
    private NavigableMap<String, Backup> history;

    public BackupsManager() {
        history = new TreeMap<>();
        clientFolders = new ArrayList<>();
    }

    public void fetchBackups() throws IOException {
        history.clear();
        clientFolders.clear();
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

    public File getBackupsFolder() {
        return backupsFolder;
    }

    public Backup getRecent() {
        Map.Entry<String, Backup> entry = history.lastEntry();
        return (entry != null) ? entry.getValue() : null;
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
        if (clientFolders.size() >= MAX_CLIENT_FOLDERS) {
            throw new ClientFoldersListFullException();
        }
        if (!clientFolders.contains(clientFolder)) {
            clientFolders.add(clientFolder);
        }
    }

    public void setClientFolders(List<File> clientFolders) {
        this.clientFolders = clientFolders;
    }

    private Backup add(Backup backup) {
        history.put(backup.id, backup);
        return backup;
    }

    public Backup makeBackup() throws IOException {
        if (!hasEnoughSpaceForBackup()) throw new NotEnoughDiskSpaceException();
        if (history.size() > 0) {
            return add(new IncrementalBackup(getRecent()));
        } else {
            return add(new BaseBackup(clientFolders, backupsFolder));
        }
    }

    public boolean hasEnoughSpaceForBackup() {
        long spaceNeeded = 0;
        for (File clientFolder : clientFolders) {
            spaceNeeded += FileUtils.sizeOfDirectory(clientFolder);
        }
        long freeSpace = backupsFolder.getFreeSpace();
        return freeSpace > spaceNeeded;
    }

    public void restore(String id, Collection<File> clientFolders) throws IOException {
        history.get(id).restore(clientFolders);
    }

    // Only delete the last one!
    public void delete(String id) throws IOException {
        File lastBackupFolder = backupsFolder.toPath().resolve(id).toFile();
        Utils.purge(lastBackupFolder);
        File modificationsFile = backupsFolder.toPath().resolve(id + "." + MODIFICATIONS_FILE_EXTENSION).toFile();
        Utils.delete(modificationsFile);
        history.remove(id);
    }

    public void flush() throws IOException {
        for (File fsNode : backupsFolder.listFiles()) {
            String id = Utils.stripExtension(fsNode.getName());
            if (!history.containsKey(id)) {
                if (fsNode.isFile()) {
                    Utils.delete(fsNode);
                } else {
                    Utils.purge(fsNode);
                }
            }
        }
    }

}
