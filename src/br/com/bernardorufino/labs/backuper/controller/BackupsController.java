package br.com.bernardorufino.labs.backuper.controller;

import br.com.bernardorufino.labs.backuper.model.backup.BackupsManager;

import java.io.File;

public class BackupsController {
    BackupsManager manager;

    public void makeBackup() {

    }

    public void restore() {

    }

    public void setUp(String backupsFolder, String clientFolder) {
        manager = new BackupsManager(new File(backupsFolder), new File(clientFolder));
    }

}
