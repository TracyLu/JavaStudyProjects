package net.madz.download.service.services;

import java.util.Arrays;
import java.util.List;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.impl.DownloadEngine;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.ListTasksRequest;
import net.madz.download.service.responses.ListTasksResponse;

@Command(commandName = "list-tasks", options = { @Option(description = "Display all the tasks.", fullName = "--all", shortName = "-a"),
        @Option(description = "Display all running tasks.", fullName = "--running", shortName = "-r"),
        @Option(description = "Display all paused tasks.", fullName = "--paused", shortName = "-p"),
        @Option(description = "Display all finished tasks.", fullName = "--finished", shortName = "-f") }, request = ListTasksRequest.class, arguments = {})
public class ListTasksService implements IService<ListTasksRequest> {

    @SuppressWarnings("unused")
    private ITelnetClient client;

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public IServiceResponse processRequest(ListTasksRequest request) throws ServiceException {
        final List<DownloadTask> tasks;
        if ( request.isAll() ) {
            tasks = Arrays.asList(DownloadEngine.getInstance().listAllTasks());
        } else if ( request.isFinished() ) {
            tasks = Arrays.asList(DownloadEngine.getInstance().findByState(IDownloadProcess.StateEnum.Finished));
        } else if ( request.isPaused() ) {
            tasks = Arrays.asList(DownloadEngine.getInstance().findByState(IDownloadProcess.StateEnum.Paused));
        } else if ( request.isRunning() ) {
            tasks = Arrays.asList(DownloadEngine.getInstance().findByState(IDownloadProcess.StateEnum.Prepared));
            List<DownloadTask> startedTasks = Arrays.asList(DownloadEngine.getInstance().findByState(IDownloadProcess.StateEnum.Started));
            for (DownloadTask task: startedTasks) {
                tasks.add(task);
            }
        } else {
            throw new ServiceException("not supported");
        }
        return new ListTasksResponse(tasks);
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