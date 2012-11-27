package net.madz.download.service;

import net.madz.download.ILifeCycle;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.service.exception.ServiceException;

public interface IService<R extends IServiceRequest> extends ILifeCycle {

    /**
     * No Exception can be thrown from this method.
     * @param request
     * 
     * @return
     * @throws ServiceException
     */
    IServiceResponse processRequest(R request) throws ServiceException;
    
    void setClient(ITelnetClient client);
}
