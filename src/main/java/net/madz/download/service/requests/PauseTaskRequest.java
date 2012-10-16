package net.madz.download.service.requests;

import java.net.URL;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.exception.ErrorException;

public class PauseTaskRequest implements IServiceRequest {

    private String url;
    private String commandName;

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void validate() throws ErrorException {
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
