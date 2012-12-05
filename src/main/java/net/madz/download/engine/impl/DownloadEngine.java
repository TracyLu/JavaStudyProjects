package net.madz.download.engine.impl;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.madz.core.lifecycle.IStateChangeListener;
import net.madz.core.lifecycle.ITransition;
import net.madz.core.lifecycle.StateContext;
import net.madz.core.lifecycle.impl.StateChangeListenerHub;
import net.madz.core.lifecycle.impl.TransitionInvocationHandler;
import net.madz.download.engine.DownloadSegment;
import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.IDownloadEngine;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.engine.IDownloadProcess.TransitionEnum;
import net.madz.download.engine.impl.metadata.MetaManager;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.requests.ResumeTaskRequest;

public class DownloadEngine implements IDownloadEngine, IStateChangeListener {

    private static final String META_SUFFIX = ".meta";
    private boolean started;
    private static DownloadEngine instance = new DownloadEngine();
    private ConcurrentHashMap<StateEnum, LinkedList<DownloadTask>> allTasks = new ConcurrentHashMap<StateEnum, LinkedList<DownloadTask>>();
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
        if ( isStarted() ) {
            return;
        }
        MetaManager.initiateMetadataDirs();
        loadAllTasks();
        // fix corrupted state from active to inactive
        // make sure all of the internal constructs of download engine started
        StateChangeListenerHub.INSTANCE.registerListener(this);
        // reschedule inactive tasks
        started = true;
    }

    private void loadAllTasks() {
        DownloadTask[] tasks = MetaManager.load("./meta");
        for (StateEnum state: StateEnum.values()) {
            allTasks.put(state, new LinkedList<DownloadTask>());
        }
        for ( DownloadTask task : tasks ) {
            StateEnum state = StateEnum.valueof(task.getState());
            allTasks.get(state).add(task);
        }
    }

    @Override
    public synchronized boolean isStarted() {
        return started;
    }

    @Override
    public synchronized void stop() {
        if ( !isStarted() ) {
            return;
        }
        pauseRunningTasks();
        StateChangeListenerHub.INSTANCE.removeListener(this);
        started = false;
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
        final StateEnum nextState = (StateEnum) context.getNextState();
        final IDownloadProcess reactiveObject = (IDownloadProcess) context.getReactiveObject();
        if ( transitionEnum == TransitionEnum.Receive && reactiveObject.getTotalLength() == reactiveObject.getReceiveBytes() ) {
            createProxy(reactiveObject).finish();
            return;
        }
        if ( nextState == StateEnum.Prepared || ( nextState == StateEnum.Started ) ) {
            activeProcesses.put(reactiveObject.getId(), createProxy(reactiveObject));
        } else {
            activeProcesses.remove(reactiveObject.getId());
        }
    }

    public DownloadSegmentWorker createSegmentWorker(IDownloadProcess downloadProcess, DownloadTask task, DownloadSegment segment) {
        return new DownloadSegmentWorker(createProxy(downloadProcess), task, segment, downloadProcess.getDataFile(), downloadProcess.getMetadataFile());
    }

    public IDownloadProcess createProxy(IDownloadProcess downloadProcess) {
        if ( downloadProcess instanceof DownloadProcess ) {
            return (IDownloadProcess) Proxy.newProxyInstance(downloadProcess.getClass().getClassLoader(), downloadProcess.getClass().getInterfaces(),
                    new TransitionInvocationHandler<IDownloadProcess, StateEnum, TransitionEnum>(downloadProcess));
        }
        return downloadProcess;
    }

    public IDownloadProcess createDownloadProcess(ResumeTaskRequest request) {
        final DownloadTask task = MetaManager.load("./meta/paused", request.getId());
        final File metadataFile = new File("./meta/paused/" + task.getFileName() + META_SUFFIX);
        return createProxy(new DownloadProcess(task, metadataFile));
    }

    public IDownloadProcess createDownloadProcess(CreateTaskRequest request) {
        final DownloadTask task = MetaManager.createDownloadTask(request);
        final File metadataFile = new File("./meta/new/" + request.getFilename() + META_SUFFIX);
        MetaManager.serializeForNewState(task, metadataFile);
        return createProxy(new DownloadProcess(task, metadataFile));
    }
}
