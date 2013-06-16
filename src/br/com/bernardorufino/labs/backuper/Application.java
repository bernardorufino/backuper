package br.com.bernardorufino.labs.backuper;

import br.com.bernardorufino.labs.backuper.controller.BackupsController;
import br.com.bernardorufino.labs.backuper.view.BackupsView;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Application {
    public static BackupsController controller;
    public static BackupsView view;
    private static ExecutorService pool;

    public static void main(String[] args) {
        pool = Executors.newCachedThreadPool();
        controller = new BackupsController();
        view = BackupsView.create();
    }

    public static void execute(Runnable runnable) {
        pool.submit(runnable);
    }



}
