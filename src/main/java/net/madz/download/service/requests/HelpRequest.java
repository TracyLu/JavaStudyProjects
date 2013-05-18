package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;

public class HelpRequest implements IServiceRequest {

    private String commandName;
    private String argCommandName;

    public HelpRequest() {
    }

    public String getArgCommandName() {
        return argCommandName;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setArgCommandName(String argCommandName) {
        this.argCommandName = argCommandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public void validate() {
    }
}
