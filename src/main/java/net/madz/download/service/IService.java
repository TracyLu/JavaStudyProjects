package net.madz.download.service;

import net.madz.download.ILifeCycle;
import net.madz.download.service.exception.ErrorException;

public interface IService<R extends IServiceRequest> extends ILifeCycle {

    /**
     * No Exception can be thrown from this method.
     * 
     * @param request
     * @return
     * @throws ErrorException
     */
    IServiceResponse processRequest(R request) throws ErrorException;
}
