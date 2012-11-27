package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ServiceException;

public class PauseTaskRequest implements IServiceRequest {

    private String url;
    private String commandName;

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void validate() throws ServiceException {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }
}
