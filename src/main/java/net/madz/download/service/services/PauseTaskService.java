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
import net.madz.download.engine.impl.DownloadEngine;
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
public class PauseTaskService implements IService<PauseTaskRequest> {

    private PauseTaskRequest request;
    @SuppressWarnings("unused")
    private ITelnetClient client;

    @Override
    public void start() {
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {
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
        int id = DownloadEngine.getInstance().pause(request.getId());
        PauseServiceResponse response = new PauseServiceResponse();
        response.setId(id);
        return response;
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }
}
