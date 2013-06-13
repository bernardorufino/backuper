package br.com.bernardorufino.labs.backuper;

import br.com.bernardorufino.labs.backuper.controller.BackupsController;
import br.com.bernardorufino.labs.backuper.view.BackupsView;

import javax.swing.*;

public class Initializer {

    public static void main(String[] args) {
        BackupsController controller = new BackupsController();
        BackupsView view = BackupsView.create(controller);
        view.build();
    }

}
