package net.madz.download.service.services;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.madz.core.lifecycle.IStateChangeListener;
import net.madz.core.lifecycle.ITransition;
import net.madz.core.lifecycle.StateContext;
import net.madz.core.lifecycle.impl.StateChangeListenerHub;
import net.madz.download.engine.DownloadProcess;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.engine.IDownloadProcess.TransitionEnum;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.requests.ListTasksRequest;
import net.madz.download.service.responses.ListTasksResponse;

@Command(commandName = "list-tasks", options = { @Option(description = "Display all the tasks.", fullName = "--all", shortName = "-a"),
        @Option(description = "Display all running tasks.", fullName = "--running", shortName = "-r"),
        @Option(description = "Display all paused tasks.", fullName = "--paused", shortName = "-p"),
        @Option(description = "Display all finished tasks.", fullName = "--finished", shortName = "-f") }, request = ListTasksRequest.class, arguments = {})
public class ListTasksService implements IService<ListTasksRequest>, IStateChangeListener {

    private ConcurrentHashMap<String, DownloadProcess> activeProcesses = new ConcurrentHashMap<String, DownloadProcess>();
    private List<DownloadTask> tasks;

    @Override
    public void start() {
        StateChangeListenerHub.INSTANCE.registerListener(this);
    }

    @Override
    public boolean isStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void stop() {
        StateChangeListenerHub.INSTANCE.removeListener(this);
    }

    @Override
    public IServiceResponse processRequest(ListTasksRequest request) throws ErrorException {
        tasks = new LinkedList<DownloadTask>();
        if ( request.isAll() ) {
            getAllTasks();
        }
        if ( request.isFinished() ) {
            getFinishedTasks();
        }
        if ( request.isPaused() ) {
            getPausedTasks();
        }
        if ( request.isRunning() ) {
            getRunningTasks();
        }
        ListTasksResponse response = new ListTasksResponse(tasks);
        return response;
    }

    private void getRunningTasks() {
        synchronized (activeProcesses) {
            System.out.println("getRunningTasks activeProcesses size:" + activeProcesses.size());
            Enumeration<String> keys = activeProcesses.keys();
            System.out.println("enter tasks size:" + tasks.size());
            while ( keys.hasMoreElements() ) {
                String url = keys.nextElement();
                System.out.println("getRunningTask url :" + url);
                if ( activeProcesses.get(url) instanceof IDownloadProcess ) {
                    DownloadProcess process = (DownloadProcess) activeProcesses.get(url);
                    System.out.println("getRunningTask task :" + process.getTask().getState());
                    tasks.add(process.getTask());
                }
            }
            System.out.println("end tasks size:" + tasks.size());
        }
    }

    private void getPausedTasks() {
        // TODO Auto-generated method stub
    }

    private void getFinishedTasks() {
        // TODO Auto-generated method stub
    }

    private void getAllTasks() {
        // TODO Auto-generated method stub
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
        synchronized (activeProcesses) {
            if ( nextState == StateEnum.Prepared || ( nextState == StateEnum.Started ) ) {
                System.out.println("ListTasksService prcess added");
                activeProcesses.put(reactiveObject.getUrl(), reactiveObject);
            } else {
                System.out.println("ListTasksService process removed");
                activeProcesses.remove(reactiveObject.getUrl());
            }
        }
        System.out.println("active processs size" + activeProcesses.size());
    }
}