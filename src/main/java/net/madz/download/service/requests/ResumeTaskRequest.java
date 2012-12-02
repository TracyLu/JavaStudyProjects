package net.madz.download.service.requests;

import java.util.List;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ExceptionMessage;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;

public class ResumeTaskRequest implements IServiceRequest {

    private String commandName;
    private String id;

    public int getId() {
        return Integer.valueOf(id);
    }

    public void setId(String id) {
        this.id = id;
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
        for ( DownloadTask task : tasks ) {
            if ( this.getId() == task.getId() ) {
                matched = true;
            }
        }
        if ( !matched ) {
            throw new ServiceException(ExceptionMessage.TASK_NOT_FOUND);
        }
    }
}
