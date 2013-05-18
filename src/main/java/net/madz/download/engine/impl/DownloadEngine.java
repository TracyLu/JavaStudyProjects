package net.madz.download.engine.impl;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

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
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.requests.ResumeTaskRequest;
import net.madz.download.utils.LogUtils;

public class DownloadEngine implements IDownloadEngine, IStateChangeListener {

    /****
     * The class DispatcherThread is responsible for taking a prepared download
     * task from preparedDownloadProcessQueue and starting it.
     * 
     * @author tracy
     * 
     */
    class DispatcherThread implements Runnable {

        @Override
        public void run() {
            while ( !Thread.currentThread().isInterrupted() ) {
                if ( dispatchedTasksCount <= MAX_TASKS_NUMBER ) {
                    IDownloadProcess proxy = null;
                    try {
                        proxy = preparedDownloadProcessQueue.take();
                        if ( null != proxy ) {
                            proxy.start();
                            dispatchedTasksCount++;
                        }
                    } catch (InterruptedException ignored) {
                        LogUtils.error(DownloadEngine.class, ignored);
                    } finally {
                        dispatcherThread = null;
                    }
                }
            }
        }
    }

    /***
     * The class PreparedThread is responsible for fetching a new task from
     * newTasksQueue, and creating a downloadProcess and a dynamic proxy to
     * prepare the task, then putting the proxy into
     * preparedDownloadProcessQueue.
     */
    class PreparedThread implements Runnable {

        @Override
        public void run() {
            while ( !Thread.currentThread().isInterrupted() ) {
                try {
                    DownloadTask task = newTasksQueue.take();
                    final File metadataFile = new File(META_FOLDER + task.getFileName() + META_SUFFIX);
                    DownloadProcess downloadProcess = new DownloadProcess(task, metadataFile);
                    IDownloadProcess proxy = DownloadEngine.getInstance().createProxy(downloadProcess);
                    proxy.prepare();
                    preparedDownloadProcessQueue.put(proxy);
                } catch (InterruptedException ignored) {
                    LogUtils.error(DownloadEngine.class, ignored);
                } finally {
                    prepareThread = null;
                }
            }
        }
    }

    public static final int MAX_TASKS_NUMBER = 10;
    public static final String META_SUFFIX = ".meta";
    public static final String META_FOLDER = "." + File.separator + "meta" + File.separator;

    public static DownloadEngine getInstance() {
        return instance;
    }

    private boolean started;
    private final static DownloadEngine instance = new DownloadEngine();
    private volatile ConcurrentHashMap<StateEnum, HashMap<Integer, DownloadTask>> allTasks = new ConcurrentHashMap<StateEnum, HashMap<Integer, DownloadTask>>();
    private volatile ConcurrentHashMap<Integer, IDownloadProcess> activeProcesses = new ConcurrentHashMap<Integer, IDownloadProcess>();
    private final BlockingQueue<DownloadTask> newTasksQueue = new LinkedBlockingQueue<DownloadTask>();
    private final BlockingQueue<IDownloadProcess> preparedDownloadProcessQueue = new ArrayBlockingQueue<IDownloadProcess>(MAX_TASKS_NUMBER);
    private volatile int dispatchedTasksCount = 0;
    private Thread prepareThread = null;
    private Thread dispatcherThread = null;

    private DownloadEngine() {
    }

    /***
     * The method check whether all segments of a task are finished.
     * 
     * @param task
     * @return
     */
    private boolean checkSegmentsAllFinished(DownloadTask task) {
        if ( null == task ) {
            return false;
        }
        for ( DownloadSegment segment : task.getSegments() ) {
            if ( segment.getCurrentBytes() != segment.getEndBytes() ) {
                return false;
            }
        }
        return true;
    }

    private IDownloadProcess createDownloadProcess(DownloadTask task) {
        final File metadataFile = new File(META_FOLDER + task.getFileName() + META_SUFFIX);
        return createProxy(new DownloadProcess(task, metadataFile));
    }

    public void createDownloadProcess(ResumeTaskRequest request) {
        final DownloadTask task = this.findById(request.getId());
        final File metadataFile = new File(META_FOLDER + task.getFileName() + META_SUFFIX);
        IDownloadProcess proxy = createProxy(new DownloadProcess(task, metadataFile));
        proxy.resume();
        System.out.println("Resumed task:");
        System.out.println(task.toString());
        preparedDownloadProcessQueue.add(proxy);
    }

    public DownloadTask createDownloadTask(CreateTaskRequest request) throws ServiceException {
        final DownloadTask task = MetaManager.createDownloadTask(request);
        allTasks.get(StateEnum.New).put(task.getId(), task);
        final File metadataFile = new File(META_FOLDER + request.getFilename() + META_SUFFIX);
        MetaManager.serializeForNewState(task, metadataFile);
        try {
            newTasksQueue.put(task);
        } catch (InterruptedException e) {
            throw new ServiceException("Meet the max workload. Please wait for several minutes and retry.");
        }
        return task;
    }

    public IDownloadProcess createProxy(IDownloadProcess downloadProcess) {
        if ( downloadProcess instanceof DownloadProcess ) {
            return (IDownloadProcess) Proxy.newProxyInstance(downloadProcess.getClass().getClassLoader(), downloadProcess.getClass().getInterfaces(),
                    new TransitionInvocationHandler<IDownloadProcess, StateEnum, TransitionEnum>(downloadProcess));
        }
        return downloadProcess;
    }

    public DownloadSegmentWorker createSegmentWorker(IDownloadProcess downloadProcess, DownloadTask task, DownloadSegment segment) {
        return new DownloadSegmentWorker(createProxy(downloadProcess), task, segment, downloadProcess.getDataFile(), downloadProcess.getMetadataFile());
    }

    @Override
    public DownloadTask findById(int id) {
        System.out.println("FindById: " + id);
        final DownloadTask[] tasks = listAllTasks();
        System.out.println("FindById tasks" + tasks.length);
        for ( DownloadTask task : tasks ) {
            System.out.println("FindById task:" + task);
            if ( id == task.getId() ) {
                return task;
            }
        }
        return null;
    }

    public synchronized DownloadTask[] findByState(IDownloadProcess.StateEnum state) {
        HashMap<Integer, DownloadTask> result = allTasks.get(state);
        return result.values().toArray(new DownloadTask[result.size()]);
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

    /***
     * The method is responsible for fixing corrupted download tasks to correct
     * state.
     * 
     * @param tasks
     * @param newState
     */
    private void fixCorrupttedTasks(DownloadTask[] tasks, StateEnum newState) {
        for ( DownloadTask task : tasks ) {
            // check whether all segments finished, but task state is still
            // started.
            //
            if ( checkSegmentsAllFinished(task) ) {
                allTasks.get(StateEnum.valueof(task.getState())).remove(task.getId());
                task.setState((byte) StateEnum.Finished.ordinal());
                allTasks.get(StateEnum.Finished).put(task.getId(), task);
                continue;
            }
            IDownloadProcess proxy = createDownloadProcess(task);
            proxy.inactivate();
        }
    }

    public ConcurrentHashMap<Integer, IDownloadProcess> getActiveProcesses() {
        return activeProcesses;
    }

    @Override
    public synchronized boolean isStarted() {
        return started;
    }

    public synchronized DownloadTask[] listAllTasks() {
        LinkedList<DownloadTask> results = new LinkedList<DownloadTask>();
        Set<Entry<StateEnum, HashMap<Integer, DownloadTask>>> entrySet = allTasks.entrySet();
        for ( Entry<StateEnum, HashMap<Integer, DownloadTask>> entry : entrySet ) {
            HashMap<Integer, DownloadTask> tasks = entry.getValue();
            for ( DownloadTask task : tasks.values() ) {
                results.add(task);
            }
        }
        return results.toArray(new DownloadTask[results.size()]);
    }

    private void loadAllTasks() {
        DownloadTask[] tasks = MetaManager.load("./meta");
        for ( StateEnum state : StateEnum.values() ) {
            allTasks.put(state, new HashMap<Integer, DownloadTask>());
        }
        for ( DownloadTask task : tasks ) {
            StateEnum state = StateEnum.valueof(task.getState());
            allTasks.get(state).put(task.getId(), task);
        }
    }

    @Override
    public void onStateChanged(StateContext<?, ?> context) {
        final ITransition transition = context.getTransition();
        final TransitionEnum transitionEnum = (TransitionEnum) transition;
        if ( !( transition instanceof TransitionEnum )
                || !( context.getReactiveObject() instanceof IDownloadProcess || !( context.getNextState() instanceof StateEnum ) ) ) {
            return;
        }
        final StateEnum currentState = (StateEnum) context.getCurrentState();
        final StateEnum nextState = (StateEnum) context.getNextState();
        final IDownloadProcess reactiveObject = (IDownloadProcess) context.getReactiveObject();
        if ( transitionEnum == TransitionEnum.Receive && reactiveObject.getTotalLength() == reactiveObject.getReceiveBytes() ) {
            createProxy(reactiveObject).finish();
            return;
        }
        if ( transitionEnum == TransitionEnum.Receive ) {
            return;
        }
        synchronized (this) {
            HashMap<Integer, DownloadTask> currentStateTasksSet = allTasks.get(currentState);
            HashMap<Integer, DownloadTask> nextStateTasksSet = allTasks.get(nextState);
            if ( currentState != nextState ) {
                if ( !nextStateTasksSet.containsKey(reactiveObject.getId()) ) {
                    nextStateTasksSet.put(reactiveObject.getId(), findById(reactiveObject.getId()));
                }
                if ( currentStateTasksSet.containsKey(reactiveObject.getId()) ) {
                    currentStateTasksSet.remove(reactiveObject.getId());
                }
                allTasks.put(currentState, currentStateTasksSet);
                allTasks.put(nextState, nextStateTasksSet);
            }
        }
        if ( nextState == StateEnum.Prepared || ( nextState == StateEnum.Started ) ) {
            activeProcesses.put(reactiveObject.getId(), createProxy(reactiveObject));
        } else {
            activeProcesses.remove(reactiveObject.getId());
            // Prepared tasks can be paused or removed directly, so we should
            // remove them from the queue.
            //
            if ( currentState == StateEnum.Prepared ) {
                preparedDownloadProcessQueue.remove(createProxy(reactiveObject));
            }
            // When a task done, we should reduce the count to let new prepared
            // tasks scheduled.
            if ( currentState == StateEnum.Started ) {
                dispatchedTasksCount--;
            }
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

    private void scheduleInactiveTasks(DownloadTask[] tasks) {
        for ( DownloadTask task : tasks ) {
            IDownloadProcess proxy = createDownloadProcess(task);
            proxy.activate();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                LogUtils.error(DownloadEngine.class, ignored);
            }
            preparedDownloadProcessQueue.add(proxy);
        }
    }

    private void scheduleNewTasks(DownloadTask[] newTasks) {
        if ( null == newTasks || 0 >= newTasks.length ) {
            return;
        }
        for ( DownloadTask task : newTasks ) {
            newTasksQueue.add(task);
        }
    }

    /***
     * When engine started, some steps need to be done, including: 1. Create
     * meta data folder if not exists, which stores information of download
     * task; 2. Register engine as a state change listener; 3. Load all download
     * tasks into ConcurrentHashMap allTasks; 4. Create and start prepare thread
     * and dispatcher thread for handing download tasks 5. Fix corrupted state
     * from active to inactive 6.Schedule new download task.
     */
    @Override
    public void start() {
        if ( isStarted() ) {
            return;
        }
        MetaManager.initiateMetadataDirs();
        StateChangeListenerHub.INSTANCE.registerListener(this);
        loadAllTasks();
        prepareThread = new Thread(new PreparedThread());
        prepareThread.setName("Prepare Thread");
        prepareThread.start();
        dispatcherThread = new Thread(new DispatcherThread());
        dispatcherThread.setName("Dispatcher Thread");
        dispatcherThread.start();
        // fix corrupted state from active to inactive
        DownloadTask[] startedTasks = findByState(StateEnum.Started);
        DownloadTask[] preparedTasks = findByState(StateEnum.Prepared);
        DownloadTask[] newTasks = findByState(StateEnum.New);
        fixCorrupttedTasks(startedTasks, StateEnum.InactiveStarted);
        scheduleInactiveTasks(findByState(StateEnum.InactiveStarted));
        fixCorrupttedTasks(preparedTasks, StateEnum.InactivePrepared);
        scheduleInactiveTasks(findByState(StateEnum.InactivePrepared));
        // Some tasks in the new blocking queue should be re-put in the queue.
        scheduleNewTasks(newTasks);
        // make sure all of the internal constructs of download engine started
        // reschedule inactive tasks
        started = true;
    }

    @Override
    public synchronized void stop() {
        if ( !isStarted() ) {
            return;
        }
        pauseRunningTasks();
        stopWorkingThread();
        StateChangeListenerHub.INSTANCE.removeListener(this);
        started = false;
    }

    private void stopWorkingThread() {
        assert null != prepareThread;
        prepareThread.interrupt();
        assert null != dispatcherThread;
        dispatcherThread.interrupt();
    }
}
