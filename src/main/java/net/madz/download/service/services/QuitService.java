package net.madz.download.service.services;

import java.util.Map.Entry;
import java.util.Set;

import demo.IDownloadProcess.StateEnum;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.connector.impl.ServiceEndpoint;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.impl.DownloadEngine;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.QuitServiceRequest;
import net.madz.download.service.responses.QuitServiceResponse;

@Command(arguments = {}, commandName = "quit", options = {}, request = QuitServiceRequest.class, description = "Quit the application.")
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
        ServiceEndpoint.getInstance().stop();
        Set<Entry<Integer, IDownloadProcess>> entrySet = PauseTaskService.activeProcesses.entrySet();
        for ( Entry<Integer, IDownloadProcess> item : entrySet ) {
            IDownloadProcess iprocess = item.getValue();
            iprocess.pause();
        }
        DownloadEngine.getInstance().stop();
        QuitServiceResponse response = new QuitServiceResponse();
        response.setMessage("bye!" + ServiceEndpoint.getInstance().isStarted());
        return response;
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }
}
