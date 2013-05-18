package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ServiceException;

public class ResumeTaskRequest implements IServiceRequest {

    private String commandName;
    private String id;

    @Override
    public String getCommandName() {
        return commandName;
    }

    public int getId() {
        return Integer.valueOf(id);
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void validate() throws ServiceException {
    }

}
