package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ServiceException;

public class ListTasksRequest implements IServiceRequest {

    private String commandName;
    private boolean all = false;
    private boolean running = false;
    private boolean paused = false;
    private boolean finished = false;

    @Override
    public String getCommandName() {
        return this.commandName;
    }

    public boolean isAll() {
        return all;
    }

    public boolean isFinished() {
        return finished;
    }
    
    public boolean isPaused() {
        return paused;
    }

    
    public boolean isRunning() {
        return running;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void validate() throws ServiceException {
        // TODO Auto-generated method stub
    }
}
