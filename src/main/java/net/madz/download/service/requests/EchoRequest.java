package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;

public class EchoRequest implements IServiceRequest {

    private String commandName;
    private String message;
    private boolean lowerCase;

    public EchoRequest() {
        super();
    }

    public String getCommandName() {
        return commandName;
    }

    public String getMessage() {
        return message;
    }

    public boolean isLowercase() {
        return lowerCase;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setL(boolean l) {
        this.lowerCase = l;
    }

    public void setLowerCase(boolean lowerCase) {
        this.lowerCase = lowerCase;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void validate() {
    }
}
