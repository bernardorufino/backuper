package br.com.bernardorufino.labs.backuper.model;

import java.io.File;
import java.util.List;

public class BackupsManager {
    private final File folder;

    public BackupsManager(File folder) {
        this.folder = folder;
    }

    public List<String> getBackups() {
        //TODO: finish
        for (File fsNode : folder.listFiles()) {

        }
        return null;
    }

}
