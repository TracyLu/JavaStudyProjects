package net.madz.download.service;

import net.madz.download.service.exception.ServiceException;

public interface IServiceRequest {

    String getCommandName();

    void validate() throws ServiceException;
}
