package net.madz.download.engine.impl;

import java.util.ArrayList;

import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.IDownloadEngine;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.impl.metadata.MetaManager;

public class DownloadEngine implements IDownloadEngine {

    private static DownloadEngine instance = new DownloadEngine();

    public static DownloadEngine getInstance() {
        return instance;
    }

    private DownloadEngine() {
    }

    @Override
    public void start() {
        MetaManager.initiateMetadataDirs();
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {
    }

    @Override
    public DownloadTask findById(int id) {
        final DownloadTask[] tasks = listAllTasks();
        for ( DownloadTask task : tasks ) {
            if ( id == task.getId() ) {
                return task;
            }
        }
        return null;
    }

    @Override
    public DownloadTask[] findByUrl(String url) {
        if ( null == url || 0 >= url.length() ) {
            return new DownloadTask[0];
        }
        final ArrayList<DownloadTask> result = new ArrayList<DownloadTask>();
        final DownloadTask[] tasks = listAllTasks();
        for ( DownloadTask task : tasks ) {
            if ( url.equalsIgnoreCase(task.getUrl().toString()) ) {
                result.add(task);
            }
        }
        return result.toArray(new DownloadTask[result.size()]);
    }

    public DownloadTask[] listAllTasks() {
        return MetaManager.load("./meta");
    }

    public DownloadTask[] findByState(IDownloadProcess.StateEnum state) {
        final ArrayList<DownloadTask> result = new ArrayList<DownloadTask>();
        final DownloadTask[] tasks = listAllTasks();
        for ( DownloadTask task : tasks ) {
            if ( state.ordinal() == task.getState() ) {
                result.add(task);
            }
        }
        return result.toArray(new DownloadTask[result.size()]);
    }
}
