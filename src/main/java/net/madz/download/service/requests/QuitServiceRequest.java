package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ServiceException;

@Command(arguments = {}, commandName = "quit", options = {}, request = QuitServiceRequest.class, description="Quit the application.")
public class QuitServiceRequest implements IServiceRequest {

    String commandName;
    
    
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void validate() throws ServiceException {
        // TODO Auto-generated method stub
    }
}
