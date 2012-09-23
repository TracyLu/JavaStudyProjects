package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;

public class HelpRequest implements IServiceRequest {

    private String commandName;
    private String argCommandName;

    public HelpRequest() {
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getArgCommandName() {
        return argCommandName;
    }

    public void setArgCommandName(String argCommandName) {
        this.argCommandName = argCommandName;
    }

    @Override
    public void validate() {
    }
}
