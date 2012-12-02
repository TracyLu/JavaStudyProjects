package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ServiceException;

public class PauseTaskRequest implements IServiceRequest {

    private String id;
    private String commandName;

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void validate() throws ServiceException {
    }

    public int getId() {
        return Integer.valueOf(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}
