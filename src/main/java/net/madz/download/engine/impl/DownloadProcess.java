package net.madz.download.engine.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.madz.download.LogUtils;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.engine.impl.metadata.DownloadTask;
import net.madz.download.engine.impl.metadata.MetaManager;
import net.madz.download.engine.impl.metadata.Segment;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.requests.ResumeTaskRequest;

public class DownloadProcess implements IDownloadProcess {

    private static final long serialVersionUID = -2404735057674043661L;
    private final DownloadTask task;
    private volatile long receiveBytes;
    private File metadataFile;
    private transient File dataFile;
    private transient final ExecutorService receiveUpdateExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean pauseFlag = false;
    private transient List<DownloadSegmentWorker> workers = new LinkedList<DownloadSegmentWorker>();
    private IDownloadProcess proxy;
    private ExecutorService localThreadPool;

    public DownloadProcess(CreateTaskRequest request) {
        super();
        this.task = MetaManager.createDownloadTask(request);
        metadataFile = new File("./meta/new/" + request.getFilename() + "_log");
        MetaManager.serializeForNewState(task, metadataFile);
    }

    public DownloadProcess(ResumeTaskRequest request) {
        super();
        this.task = MetaManager.load("./meta/paused", request.getId());
        metadataFile = new File("./meta/paused/" + this.task.getFileName() + "_log");
        this.receiveBytes = this.task.getReceivedBytes();
        System.out.println("Received Bytes:" + this.receiveBytes);
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
        metadataFile = MetaManager.move(metadataFile, new File("./meta/prepared"));
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
        metadataFile = MetaManager.move(metadataFile, new File("./meta/started"));
        final List<Segment> segments = task.getSegments();
        localThreadPool = Executors.newFixedThreadPool(task.getThreadNumber());
        for ( final Segment segment : segments ) {
            if ( pauseFlag ) {
                break;
            }
            if ( segment.getCurrentBytes() < segment.getEndBytes() ) {
                DownloadSegmentWorker worker = new DownloadSegmentWorker(proxy, task, segment, dataFile, metadataFile);
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
        if ( (byte) StateEnum.Started.ordinal() == task.getState() ) {
            task.setState((byte) StateEnum.InactiveStarted.ordinal());
            MetaManager.move(metadataFile, new File("./meta/inactiveStarted"));
        } else if ( (byte) StateEnum.Prepared.ordinal() == task.getState() ) {
            task.setState((byte) StateEnum.InactivePrepared.ordinal());
            MetaManager.move(metadataFile, new File("./meta/inactivePrepared"));
        }
        resetProcessWhenNotResumable();
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
        MetaManager.move(metadataFile, new File("./meta/prepared"));
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
        metadataFile = MetaManager.move(metadataFile, new File("./meta/paused"));
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
        System.out.println("============Enter finish===========");
        metadataFile = MetaManager.move(metadataFile, new File("./meta/finished"));
        this.receiveUpdateExecutor.shutdown();
        this.localThreadPool.shutdown();
    }

    @Override
    public void err() {
        task.setState((byte) StateEnum.Failed.ordinal());
        metadataFile = MetaManager.move(metadataFile, new File("./meta/failed"));
        this.receiveUpdateExecutor.shutdown();
        this.localThreadPool.shutdown();
    }

    @Override
    public void remove(boolean both) {
        receiveUpdateExecutor.shutdown();
        dataFile.delete();
        metadataFile.delete();
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
        metadataFile = MetaManager.move(metadataFile, new File("./meta/prepared"));
        File folder = task.getFolder();
        dataFile = new File(folder, task.getFileName());
    }

    public synchronized long getReceiveBytes() {
        return receiveBytes;
    }

    public synchronized DownloadTask getTask() {
        return task;
    }

    public void setProxy(IDownloadProcess proxy) {
        this.proxy = proxy;
    }

    @Override
    public int getTaskId() {
        return this.task.getId();
    }

    public synchronized IDownloadProcess getProxy() {
        return proxy;
    }

    @Override
    public boolean isPaused() {
        return pauseFlag;
    }
}
