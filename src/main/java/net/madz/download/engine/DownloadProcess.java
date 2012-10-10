package net.madz.download.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.madz.core.lifecycle.IState;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;
import net.madz.download.service.metadata.Segment;
import net.madz.download.service.requests.CreateTaskRequest;

public class DownloadProcess implements IDownloadProcess {

    private static final long serialVersionUID = -2404735057674043661L;
    private CreateTaskRequest request;
    private DownloadTask task;
    private transient ExecutorService threadPool = null;
    private File metadataFile;
    private File dataFile;
    private DownloadTaskWorker worker;

    public DownloadProcess(CreateTaskRequest request) {
        super();
        this.request = request;
        metadataFile = new File("./meta/new/" + request.getFilename() + "_log");
        MetaManager.serializeForNewState(request, metadataFile);
        threadPool = Executors.newFixedThreadPool(10);
    }

    @Override
    public <S extends IState> S getState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void prepare() {
        task = MetaManager.createDownloadTask(request);
        MetaManager.serializeForPreparedState(task, metadataFile);
        MetaManager.computeSegmentsInformation(task);
        MetaManager.serializeSegmentsInformation(task, metadataFile);
        MetaManager.move(metadataFile, new File("./meta/prepared"));
        File folder = task.getFolder();
        dataFile = new File(folder, task.getFileName());
    }

    @Override
    public void start() {
        worker = new DownloadTaskWorker(dataFile, metadataFile, task);
        threadPool.execute(worker);
    }

    @Override
    public void receive(long bytes) {
        // TODO Auto-generated method stub
    }

    @Override
    public void inactivate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void activate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void pause() {
        worker.pause();
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
    }

    @Override
    public void err() {
        // TODO Auto-generated method stub
    }

    @Override
    public void remove(boolean both) {
        // TODO Auto-generated method stub
    }

    @Override
    public void restart() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }
}
