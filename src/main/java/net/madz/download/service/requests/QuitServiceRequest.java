package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ServiceException;

public class QuitServiceRequest implements IServiceRequest {

    String commandName;
    
    
    @Override
    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public void validate() throws ServiceException {
        // TODO Auto-generated method stub
    }
}
