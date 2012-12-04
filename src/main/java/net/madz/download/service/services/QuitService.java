package net.madz.download.service.services;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.QuitServiceRequest;
import net.madz.download.service.responses.QuitServiceResponse;

public class QuitService implements IService<QuitServiceRequest> {

    boolean started;
    private ITelnetClient client;

    @Override
    public void start() {
        started = true;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public IServiceResponse processRequest(QuitServiceRequest request) throws ServiceException {
        QuitServiceResponse response = new QuitServiceResponse();
        return response;
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }
}
