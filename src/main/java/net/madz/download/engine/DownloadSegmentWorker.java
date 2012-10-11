package net.madz.download.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;
import net.madz.download.service.metadata.Segment;

public final class DownloadSegmentWorker implements Runnable {

    private final DownloadTask task;
    private final Segment segment;
    private final Lock poolLock;
    private final Condition allDone;
    private boolean pauseFlag;
    private DownloadProcess process;
    private File dataFile;
    private File metadataFile;

    public DownloadSegmentWorker(DownloadProcess process, DownloadTask task, Segment segment, Lock poolLock, Condition allDone, File dataFile, File metadataFile) {
        this.process = process;
        this.task = task;
        this.segment = segment;
        this.poolLock = poolLock;
        this.allDone = allDone;
        this.dataFile = dataFile;
        this.metadataFile = metadataFile;
    }

    @Override
    public void run() {
        URL url = task.getUrl();
        HttpURLConnection openConnection = null;
        InputStream inputStream = null;
        RandomAccessFile randomAccessDataFile = null;
        byte[] buf = new byte[8096];
        int size = 0;
        try {
            openConnection = (HttpURLConnection) url.openConnection();
            openConnection.setRequestProperty("RANGE", "bytes=" + segment.getStartBytes() + "-" + segment.getEndBytes());
            openConnection.connect();
            inputStream = openConnection.getInputStream();
            randomAccessDataFile = new RandomAccessFile(dataFile, "rw");
            long off = segment.getStartBytes();
            System.out.println("£«£«£«£«£«£«£«£«Downloading segment" + segment.getId() + " off:" + off);
            while ( ( !pauseFlag ) && ( size = inputStream.read(buf) ) != -1 ) {
                randomAccessDataFile.seek(off);
                randomAccessDataFile.write(buf, 0, size);
                process.receive(size);
                off += size;
                System.out.println("=====Downloading segment" + segment.getId() + " off:" + off);
                MetaManager.updateSegmentDownloadProgress(metadataFile, segment.getId(), off);
            }
//            if ( !pauseFlag ) {
//                poolLock.lock();
//                try {
//                    process.increaseDoneNumber();
//                    allDone.signalAll();
//                } finally {
//                    poolLock.unlock();
//                }
//            }
        } catch (IOException e) {
        } finally {
            // try {
            // if ( null != inputStream ) {
            // inputStream.close();
            // }
            // randomAccessDataFile.close();
            // randomAccessMetadataFile.close();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
        }
    }

    public boolean isPauseFlag() {
        return pauseFlag;
    }

    public synchronized void setPauseFlag(boolean pauseFlag) {
        this.pauseFlag = pauseFlag;
    }
}
