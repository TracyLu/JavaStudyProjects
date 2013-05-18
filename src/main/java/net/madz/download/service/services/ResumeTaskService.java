package net.madz.download.service.services;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.impl.DownloadEngine;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.ResumeTaskRequest;
import net.madz.download.service.responses.ResumeTaskResponse;

@Command(arguments = { @Arg(name = "id", description = "the id of task") }, commandName = "resume-task", options = {}, request = ResumeTaskRequest.class,
        description = "Resume a paused download task by specifing the task id.")
public class ResumeTaskService implements IService<ResumeTaskRequest> {

    @SuppressWarnings("unused")
    private ITelnetClient client;

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public IServiceResponse processRequest(ResumeTaskRequest request) throws ServiceException {
        DownloadEngine.getInstance().createDownloadProcess(request);
        return new ResumeTaskResponse("You task is resumed to download.");
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
