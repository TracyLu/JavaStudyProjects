package net.madz.download.engine.impl.metadata;

public class IdFactory {

    private static int id = 0;
    private static final IdFactory instance = new IdFactory();

    private IdFactory() {
    }

    public static synchronized IdFactory getInstance() {
        return instance;
    }

    public synchronized int generate() {
        id = id + 1;
        return id;
    }
}
