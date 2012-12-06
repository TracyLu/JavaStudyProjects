package net.madz.download.engine.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.madz.download.engine.DownloadSegment;
import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.impl.metadata.MetaManager;
import net.madz.download.utils.FileUtils;
import net.madz.download.utils.LogUtils;

public class DownloadProcess implements IDownloadProcess {

    private static final String META_FOLDER = "./meta/";
    private static final long serialVersionUID = -2404735057674043661L;
    private final DownloadTask task;
    private volatile long receiveBytes;
    private File metadataFile;
    private transient File dataFile;
    private transient final ExecutorService receiveUpdateExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean pauseFlag = false;
    private transient List<DownloadSegmentWorker> workers = new LinkedList<DownloadSegmentWorker>();
    private ExecutorService localThreadPool;

    public DownloadProcess(DownloadTask task, File metadataFile) {
        this.task = task;
        this.metadataFile = metadataFile;
        this.receiveBytes = this.task.getReceivedBytes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public StateEnum getState() {
        int stateIndex = task.getState();
        StateEnum[] states = StateEnum.values();
        return states[stateIndex];
    }

    void setState(StateEnum state) throws FileNotFoundException, IOException {
        this.task.setState((byte) state.ordinal());
        MetaManager.updateTaskState(metadataFile, state);
    }

    @Override
    public void prepare() {
        MetaManager.serializeForPreparedState(task, metadataFile);
        MetaManager.computeSegmentsInformation(task);
        MetaManager.serializeSegmentsInformation(task, metadataFile);
        File folder = task.getFolder();
        dataFile = new File(folder, task.getFileName());
        try {
            dataFile.createNewFile();
        } catch (IOException ignored) {
            LogUtils.error(DownloadProcess.class, ignored);
        }
    }

    @Override
    public void start() {
        final List<DownloadSegment> segments = task.getSegments();
        localThreadPool = Executors.newFixedThreadPool(task.getThreadNumber());
        for ( final DownloadSegment segment : segments ) {
            if ( pauseFlag ) {
                break;
            }
            if ( segment.getCurrentBytes() < segment.getEndBytes() ) {
                DownloadSegmentWorker worker = DownloadEngine.getInstance().createSegmentWorker(this, task, segment);
                workers.add(worker);
                localThreadPool.submit(worker);
            }
        }
    }

    @Override
    public void receive(final long bytes) {
        receiveUpdateExecutor.submit(new Runnable() {

            @Override
            public void run() {
                DownloadProcess.this.receiveBytes += bytes;
                System.out.println("Received bytes:" + bytes + " Total Length:" + task.getTotalLength());
                synchronized (DownloadProcess.this) {
                    if ( receiveBytes == task.getTotalLength() ) {
                        DownloadProcess.this.notify();
                    }
                }
            }
        });
    }

    @Override
    public void inactivate() {
        resetProcessWhenNotResumable();
        if ( (byte) StateEnum.Started.ordinal() == task.getState() ) {
            for ( DownloadSegment segment : task.getSegments() ) {
                if ( segment.getState() == StateEnum.Started.ordinal() ) {
                    segment.setState((byte) StateEnum.InactiveStarted.ordinal());
                }
            }
        } else if ( (byte) StateEnum.Prepared.ordinal() == task.getState() ) {
            for ( DownloadSegment segment : task.getSegments() ) {
                if ( segment.getState() == StateEnum.Prepared.ordinal() ) {
                    segment.setState((byte) StateEnum.InactivePrepared.ordinal());
                }
            }
        }
    }

    private void resetProcessWhenNotResumable() {
        if ( !task.isResumable() ) {
            try {
                MetaManager.updateSegmentDownloadProgress(metadataFile, 0, 0);
            } catch (FileNotFoundException ignored) {
                LogUtils.error(DownloadProcess.class, ignored);
            } catch (IOException ignored) {
                LogUtils.error(DownloadProcess.class, ignored);
            }
        }
    }

    @Override
    public void activate() {
        if ( (byte) StateEnum.InactiveStarted.ordinal() == task.getState() ) {
            for ( DownloadSegment segment : task.getSegments() ) {
                if ( segment.getState() == StateEnum.InactiveStarted.ordinal() ) {
                    segment.setState((byte) StateEnum.Prepared.ordinal());
                }
            }
        } else if ( (byte) StateEnum.InactivePrepared.ordinal() == task.getState() ) {
            for ( DownloadSegment segment : task.getSegments() ) {
                if ( segment.getState() == StateEnum.InactivePrepared.ordinal() ) {
                    segment.setState((byte) StateEnum.Prepared.ordinal());
                }
            }
        }
        File folder = task.getFolder();
        dataFile = new File(folder, task.getFileName());
    }

    @Override
    public void pause() {
        this.pauseFlag = true;
        this.receiveUpdateExecutor.shutdownNow();
        this.localThreadPool.shutdownNow();
        try {
            while ( !this.localThreadPool.isTerminated() ) {
                synchronized (this) {
                    wait(500L);
                }
            }
        } catch (InterruptedException ignored) {
        }
        resetProcessWhenNotResumable();
        try {
            MetaManager.updateSegmentState(metadataFile, task, StateEnum.Paused);
        } catch (FileNotFoundException ignored) {
            LogUtils.debug(DownloadProcess.class, ignored.getMessage());
        } catch (IOException ignored) {
            LogUtils.debug(DownloadProcess.class, ignored.getMessage());
        }
    }

    @Override
    public void finish() {
        this.receiveUpdateExecutor.shutdown();
        this.localThreadPool.shutdown();
    }

    @Override
    public void err() {
        task.setState((byte) StateEnum.Failed.ordinal());
        this.receiveUpdateExecutor.shutdown();
        this.localThreadPool.shutdown();
    }

    @Override
    public void remove(boolean both) {
        receiveUpdateExecutor.shutdown();
        FileUtils.delete(dataFile);
        FileUtils.delete(metadataFile);
    }

    @Override
    public void restart() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        try {
            MetaManager.updateSegmentState(metadataFile, task, StateEnum.Prepared);
        } catch (FileNotFoundException ignored) {
            LogUtils.error(DownloadProcess.class, ignored);
        } catch (IOException ignored) {
            LogUtils.error(DownloadProcess.class, ignored);
        }
        File folder = task.getFolder();
        dataFile = new File(folder, task.getFileName());
    }

    public synchronized long getReceiveBytes() {
        return receiveBytes;
    }

    public synchronized DownloadTask getTask() {
        return task;
    }

    @Override
    public int getId() {
        return this.task.getId();
    }

    @Override
    public boolean isPaused() {
        return pauseFlag;
    }

    @Override
    public File getMetadataFile() {
        return metadataFile;
    }

    @Override
    public File getDataFile() {
        return dataFile;
    }

    @Override
    public long getTotalLength() {
        return this.task.getTotalLength();
    }
}
