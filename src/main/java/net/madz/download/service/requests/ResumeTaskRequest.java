package net.madz.download.service.requests;

import java.util.List;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ExceptionMessage;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;

public class ResumeTaskRequest implements IServiceRequest {

    private String commandName;
    private String taskName;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void validate() throws ServiceException {
        List<DownloadTask> tasks = MetaManager.load("./meta/paused");
        boolean matched = false;
        for (DownloadTask task: tasks) {
            if (taskName.equalsIgnoreCase(task.getFileName())) {
                matched = true;
            }
        }
        if (!matched) {
            throw new ServiceException(ExceptionMessage.TASK_NOT_FOUND);
        }
    }
}
