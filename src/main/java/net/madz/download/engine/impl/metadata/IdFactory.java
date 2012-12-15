package net.madz.download.engine.impl.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import net.madz.download.utils.LogUtils;

public class IdFactory implements Serializable {

    private static final long serialVersionUID = -1505732795427201228L;
    private static final File file = new File("./id.meta");
    private static final IdFactory instance = new IdFactory();
    private static final int MAX_ID_NUMBER = 50;
    private int topNumber = 0;
    private volatile int count = 0;

    public static synchronized IdFactory getInstance() {
        return instance;
    }

    public synchronized int getId() {
        if ( 0 == count || count >= MAX_ID_NUMBER ) {
            topNumber = generate();
            count = 0;
        }
        count++;
        return topNumber - MAX_ID_NUMBER + count;
    }

    private IdFactory() {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            if ( 0 == raf.length() ) {
                raf.seek(0);
                raf.writeInt(MAX_ID_NUMBER);
            }
        } catch (FileNotFoundException e) {
            LogUtils.error(IdFactory.class, e);
        } catch (IOException e) {
            LogUtils.error(IdFactory.class, e);
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                LogUtils.error(IdFactory.class, e);
            }
        }
    }

    private int generate() {
        System.out.println("generate");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.seek(0);
            topNumber = raf.readInt();
            raf.seek(0);
            raf.writeInt(topNumber + MAX_ID_NUMBER);
        } catch (FileNotFoundException e) {
            LogUtils.error(IdFactory.class, e);
        } catch (IOException e) {
            LogUtils.error(IdFactory.class, e);
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                LogUtils.error(IdFactory.class, e);
            }
        }
        return topNumber;
    }
}
