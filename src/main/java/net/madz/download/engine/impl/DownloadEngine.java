package net.madz.download.engine.impl;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.madz.core.lifecycle.IStateChangeListener;
import net.madz.core.lifecycle.ITransition;
import net.madz.core.lifecycle.StateContext;
import net.madz.core.lifecycle.impl.StateChangeListenerHub;
import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.IDownloadEngine;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.engine.IDownloadProcess.TransitionEnum;
import net.madz.download.engine.impl.metadata.MetaManager;

public class DownloadEngine implements IDownloadEngine, IStateChangeListener {

    private boolean started;
    private static DownloadEngine instance = new DownloadEngine();
    private ConcurrentHashMap<Integer, IDownloadProcess> activeProcesses = new ConcurrentHashMap<Integer, IDownloadProcess>();

    public static DownloadEngine getInstance() {
        return instance;
    }

    public ConcurrentHashMap<Integer, IDownloadProcess> getActiveProcesses() {
        return activeProcesses;
    }

    private DownloadEngine() {
    }

    @Override
    public synchronized void start() {
        if ( !isStarted() ) {
            MetaManager.initiateMetadataDirs();
            StateChangeListenerHub.INSTANCE.registerListener(this);
            started = true;
        }
    }

    @Override
    public synchronized boolean isStarted() {
        return started;
    }

    @Override
    public synchronized void stop() {
        if ( isStarted() ) {
            pauseRunningTasks();
            StateChangeListenerHub.INSTANCE.removeListener(this);
            started = false;
        }
    }

    public int pause(int taskId) {
        final IDownloadProcess proxy = activeProcesses.get(taskId);
        if ( null == proxy ) {
            return -1;
        }
        proxy.pause();
        return taskId;
    }

    public Integer[] pauseRunningTasks() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        Set<Entry<Integer, IDownloadProcess>> entrySet = activeProcesses.entrySet();
        for ( Entry<Integer, IDownloadProcess> entry : entrySet ) {
            pause(entry.getKey());
            ids.add(entry.getKey().intValue());
        }
        return ids.toArray(new Integer[ids.size()]);
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

    @Override
    public void onStateChanged(StateContext<?, ?> context) {
        final ITransition transition = context.getTransition();
        final TransitionEnum transitionEnum = (TransitionEnum) transition;
        if ( !( transition instanceof TransitionEnum )
                || !( context.getReactiveObject() instanceof IDownloadProcess || !( context.getNextState() instanceof StateEnum ) ) ) {
            return;
        }
        if ( transitionEnum == TransitionEnum.Receive ) {
            return;
        }
        System.out.println("Current State:" + context.getCurrentState());
        System.out.println("Next State:" + context.getNextState());
        System.out.println("Transition:" + context.getTransition());
        final StateEnum nextState = (StateEnum) context.getNextState();
        final DownloadProcess reactiveObject = (DownloadProcess) context.getReactiveObject();
        if ( nextState == StateEnum.Prepared || ( nextState == StateEnum.Started ) ) {
            System.out.println("DownloadEngine process added");
            activeProcesses.put(reactiveObject.getTask().getId(), reactiveObject.getProxy());
        } else {
            System.out.println("DownloadEngine process removed");
            activeProcesses.remove(reactiveObject.getTask().getId());
        }
    }
}
