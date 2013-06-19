package br.com.bernardorufino.labs.backuper.controller;

import java.io.IOException;

public abstract class Transaction {

    public static class RollbackException extends RuntimeException { /* Empty */ }

    public abstract void commit() throws Throwable;

    public abstract void rollback(Throwable e) throws Throwable;

    public boolean execute() {
        try {
            commit();
            return true;
        } catch (Throwable commitException) {
            System.out.println("caught commitException");
            try {
                rollback(commitException);
                return false;
            } catch (Throwable rollbackException) {
                System.out.println("caught rollbackException");
                RollbackException e = new RollbackException();
                e.initCause(rollbackException);
                throw e;
            }
        }
    }

}
