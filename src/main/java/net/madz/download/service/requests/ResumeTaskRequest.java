package net.madz.download.service.requests;

import java.util.List;

import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.impl.metadata.MetaManager;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ExceptionMessage;
import net.madz.download.service.exception.ServiceException;

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
    }

}
