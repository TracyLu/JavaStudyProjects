package net.madz.download.service.services;

import java.util.Arrays;
import java.util.List;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.impl.DownloadEngine;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.responses.CreateTaskResponse;

@Command(arguments = { @Arg(name = "url", description = "the address of remote file."), @Arg(name = "folder", description = "where to store the file"),
        @Arg(name = "filename", description = "new file name.") }, commandName = "create-task", options = {
        @Option(description = "thread number", fullName = "--threadNumber", shortName = "-n"),
        @Option(description = "ReCreate a new download task", fullName = "--reCreate", shortName = "-r") }, request = CreateTaskRequest.class,
        description = "This command is responsible for downloding specified url resource.")
public class CreateTaskService implements IService<CreateTaskRequest> {

    private ITelnetClient client;

    private String generateNewName(String folderName, String oldName, List<DownloadTask> tasks) {
        String newName = null;
        boolean match = false;
        for ( DownloadTask task : tasks ) {
            if ( oldName.equalsIgnoreCase(task.getFileName()) ) {
                if ( folderName.equalsIgnoreCase(task.getFolder().toString()) ) {
                    match = true;
                }
            }
        }
        if ( match ) {
            String[] results = oldName.split("\\.");
            if ( results.length == 1 ) {
                newName = results.toString().concat("1");
            } else if ( results.length == 2 ) {
                newName = results[0].concat(".").concat("1").concat(".").concat(results[1]);
            } else if ( results.length == 3 ) {
                // if the results[length - 2] is not a number, then add .1
                // else results[length - 2] ++
                // TODO
                int number = Integer.valueOf(results[1]).intValue();
                int newNumber = number + 1;
                newName = results[0].concat(".").concat(newNumber + "").concat(".").concat(results[2]);
            }
            newName = generateNewName(folderName, newName, tasks);
        } else {
            newName = oldName;
        }
        return newName;
    }

    private String getNewFileName(String folderName, String filename) {
        String newName = null;
        newName = generateNewName(folderName, filename, Arrays.asList(DownloadEngine.getInstance().listAllTasks()));
        return newName;
    }

    private boolean isDownloaded(CreateTaskRequest request) {
        final DownloadTask[] tasks = DownloadEngine.getInstance().findByUrl(request.getUrl());
        return tasks.length > 0;
    }

    @Override
    public boolean isStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IServiceResponse processRequest(CreateTaskRequest request) throws ServiceException {
        if ( null == request || null == request.getUrl() ) {
            throw new ServiceException("Create task request or URL should not be null.");
        }
        if ( isDownloaded(request) && !request.isReCreate() ) {
            final String confirmation = "The task is finished. Do you want to create a new task? (Y:N): ";
            String response = null;
            do {
                response = client.acquireConfirm(confirmation);
            } while ( null == response || 0 >= response.length() );
            if ( !"Y".equalsIgnoreCase(response) ) {
                return new CreateTaskResponse("Request ignored.");
            } else {
                request.setFilename(getNewFileName(request.getFolder(), request.getFilename()));
            }
        } else if ( isDownloaded(request) && request.isReCreate() ) {
            request.setFilename(getNewFileName(request.getFolder(), request.getFilename()));
        }
        DownloadTask task = DownloadEngine.getInstance().createDownloadTask(request);
        return new CreateTaskResponse(String.valueOf(task.getId()));
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }
}
