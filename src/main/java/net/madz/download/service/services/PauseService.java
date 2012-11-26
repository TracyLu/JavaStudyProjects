package net.madz.download.service.services;

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
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.requests.PauseTaskRequest;
import net.madz.download.service.responses.PauseServiceResponse;

@Command(arguments = { @Arg(name = "url", description = "the address of remote file.") }, commandName = "pause-task", request = PauseTaskRequest.class,
        description = "This command is responsible for pausing a running task.", options = {})
public class PauseService implements IService<PauseTaskRequest>, IStateChangeListener {

    static ConcurrentHashMap<String, IDownloadProcess> activeProcesses = new ConcurrentHashMap<String, IDownloadProcess>();
    private PauseTaskRequest request;

    @Override
    public void start() {
        StateChangeListenerHub.INSTANCE.registerListener(this);
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {
        StateChangeListenerHub.INSTANCE.removeListener(this);
    }

    public PauseTaskRequest getRequest() {
        return request;
    }

    public void setRequest(PauseTaskRequest request) {
        this.request = request;
    }

    @Override
    public IServiceResponse processRequest(PauseTaskRequest request) throws ErrorException {
        System.out.println("Enter PauseService");
        final IDownloadProcess proxy = activeProcesses.get(request.getUrl());
        proxy.pause();
        PauseServiceResponse response = new PauseServiceResponse();
        response.setUrl(proxy.getUrl());
        return response;
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
            System.out.println("PauseService process added");
            activeProcesses.put(reactiveObject.getUrl(), reactiveObject.getProxy());
        } else {
            System.out.println("PauseService process removed");
            activeProcesses.remove(reactiveObject.getUrl());
        }
    }
}
