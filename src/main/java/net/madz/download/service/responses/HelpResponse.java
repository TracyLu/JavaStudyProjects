package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;

public class HelpResponse implements IServiceResponse {

    private String commandName;
    private String description;

    public String getCommandName() {
        return commandName;
    }

    public String getDescription() {
        return description;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
