package net.madz.download.service.services;

import java.util.concurrent.ConcurrentHashMap;

import net.madz.core.lifecycle.IStateChangeListener;
import net.madz.core.lifecycle.ITransition;
import net.madz.core.lifecycle.StateContext;
import net.madz.core.lifecycle.impl.StateChangeListenerHub;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.engine.IDownloadProcess.TransitionEnum;
import net.madz.download.engine.impl.DownloadProcess;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.PauseTaskRequest;
import net.madz.download.service.responses.PauseServiceResponse;

@Command(arguments = { @Arg(name = "id", description = "the id of task.") }, commandName = "pause-task", request = PauseTaskRequest.class,
        description = "Pause a running (in prepared or started state) task.", options = {})
public class PauseTaskService implements IService<PauseTaskRequest>, IStateChangeListener {

    static ConcurrentHashMap<Integer, IDownloadProcess> activeProcesses = new ConcurrentHashMap<Integer, IDownloadProcess>();
    private PauseTaskRequest request;
    @SuppressWarnings("unused")
    private ITelnetClient client;

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
    public IServiceResponse processRequest(PauseTaskRequest request) throws ServiceException {
        System.out.println("Enter PauseService");
        final IDownloadProcess proxy = activeProcesses.get(request.getId());
        proxy.pause();
        PauseServiceResponse response = new PauseServiceResponse();
        response.setId(proxy.getTaskId());
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
            activeProcesses.put(reactiveObject.getTask().getId(), reactiveObject.getProxy());
        } else {
            System.out.println("PauseService process removed");
            activeProcesses.remove(reactiveObject.getTask().getId());
        }
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }
}
