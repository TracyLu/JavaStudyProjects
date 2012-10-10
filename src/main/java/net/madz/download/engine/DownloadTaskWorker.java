package net.madz.download.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.madz.download.LogUtils;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;
import net.madz.download.service.metadata.Segment;

public class DownloadTaskWorker implements Runnable {

    private DownloadTask task;
    private File dataFile;
    private File metadataFile;
    private boolean pauseFlag = false;
    private ExecutorService localThreadPool = Executors.newFixedThreadPool(5);

    public DownloadTaskWorker(File dataFile, File metadataFile, DownloadTask task) {
        this.task = task;
        this.dataFile = dataFile;
        this.metadataFile = metadataFile;
    }

    public void pause() {
        pauseFlag = true;
    }

    @Override
    public void run() {
        task.setState((byte) StateEnum.Started.ordinal());
        MetaManager.move(metadataFile, new File("./meta/started"));
        try {
            MetaManager.updateTaskState(metadataFile, StateEnum.Started);
        } catch (FileNotFoundException ignored) {
            LogUtils.debug(DownloadTaskWorker.class, ignored.getMessage());
        } catch (IOException ignored) {
            LogUtils.debug(DownloadTaskWorker.class, ignored.getMessage());
        }
        new Runnable() {

            @Override
            public void run() {
                execute(task);
            }

            private void execute(final DownloadTask task) {
                List<Segment> segments = task.getSegments();
                final Lock poolLock = new ReentrantLock();
                final Condition allDone = poolLock.newCondition();
                int doneNumber = 0;
                for ( final Segment segment : segments ) {
                    if ( !pauseFlag ) {
                        localThreadPool.execute(new Runnable() {

                            @Override
                            public void run() {
                                executeBySegment(segment);
                            }

                            private void executeBySegment(Segment segment) {
                                URL url = task.getUrl();
                                HttpURLConnection openConnection = null;
                                InputStream inputStream = null;
                                RandomAccessFile randomAccessDataFile = null;
                                RandomAccessFile randomAccessMetadataFile = null;
                                byte[] buf = new byte[8096];
                                int doneNumber = 0;
                                int size = 0;
                                try {
                                    openConnection = (HttpURLConnection) url.openConnection();
                                    openConnection.setRequestProperty("RANGE", "bytes=" + segment.getStartBytes() + "-" + segment.getEndBytes());
                                    openConnection.connect();
                                    inputStream = openConnection.getInputStream();
                                    randomAccessDataFile = new RandomAccessFile(dataFile, "rw");
                                    long off = segment.getStartBytes();
                                    randomAccessMetadataFile = new RandomAccessFile(metadataFile, "rw");
                                    while ( ( !pauseFlag ) && ( size = inputStream.read(buf) ) != -1 ) {
                                        randomAccessDataFile.seek(off);
                                        randomAccessDataFile.write(buf, 0, size);
                                        off += size;
                                        MetaManager.updateSegmentDownloadProgress(metadataFile, segment.getId(), off);
                                    }
                                    if ( !pauseFlag ) {
                                        poolLock.lock();
                                        try {
                                            doneNumber++;
                                            allDone.signalAll();
                                        } finally {
                                            poolLock.unlock();
                                        }
                                    }
                                } catch (IOException e) {
                                } finally {
                                    try {
                                        inputStream.close();
                                        randomAccessDataFile.close();
                                        randomAccessMetadataFile.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
                if ( !pauseFlag ) {
                    poolLock.lock();
                    try {
                        while ( doneNumber < segments.size() ) {
                            try {
                                allDone.await();
                                if ( doneNumber == segments.size() ) {
                                    localThreadPool.shutdown();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        poolLock.unlock();
                    }
                } else {
                    localThreadPool.shutdown();
                }
            }
        };
    }
}