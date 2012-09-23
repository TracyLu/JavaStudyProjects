package net.madz.download.service;

import net.madz.download.service.exception.ErrorException;

public interface IServiceRequest {

    String getCommandName();

    void validate() throws ErrorException;
}
