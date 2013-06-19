package br.com.bernardorufino.labs.backuper;

import br.com.bernardorufino.labs.backuper.controller.BackupsManager;
import br.com.bernardorufino.labs.backuper.view.BackupsView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application {
    public static BackupsManager controller;
    public static BackupsView view;
    private static ExecutorService pool;

    public static void main(String[] args) {
        pool = Executors.newCachedThreadPool();
        controller = new BackupsManager();
        view = BackupsView.create();
    }

    public static void execute(Runnable runnable) {
        pool.submit(runnable);
    }



}
