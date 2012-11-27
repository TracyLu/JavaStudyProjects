package net.madz.download.service.services;

import java.lang.reflect.Proxy;
import java.util.List;

import net.madz.core.lifecycle.IStateChangeListener;
import net.madz.core.lifecycle.StateContext;
import net.madz.core.lifecycle.impl.StateChangeListenerHub;
import net.madz.core.lifecycle.impl.TransitionInvocationHandler;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.agent.impl.TelnetClient;
import net.madz.download.engine.DownloadProcess;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.engine.IDownloadProcess.TransitionEnum;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.responses.CreateTaskResponse;

@Command(arguments = { @Arg(name = "url", description = "the address of remote file."), @Arg(name = "folder", description = "where to store the file"),
        @Arg(name = "filename", description = "new file name.") }, commandName = "create-task", options = { @Option(description = "thread number",
        fullName = "--threadNumber", shortName = "-n") }, request = CreateTaskRequest.class,
        description = "This command is responsible for downloding specified url resource.")
public class CreateTaskService implements IService<CreateTaskRequest>, IStateChangeListener {

    private DownloadProcess process;
    private IDownloadProcess iProcess;
    private ITelnetClient client;

    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    @Override
    public IServiceResponse processRequest(CreateTaskRequest request) throws ServiceException {
        if ( null == request || null == request.getUrl() ) {
            throw new ServiceException("Create task request or URL should not be null.");
        }
        CreateTaskResponse downloadResponse = new CreateTaskResponse();
        if ( checkWhetherDownloaded(request) ) {
            String output = "The task is finished. Do you want to reload the task? (Y:N): ";
            TelnetClient realClient = (TelnetClient) client;
            String response = realClient.acquireConfirm(output);
            while ( null == response || 0 >= response.length() ) {
                response = realClient.acquireConfirm(output);
            }
            if ( "Y".equalsIgnoreCase(response) ) {
                // Change the filename if equals to the older one.
                //
                String newName = getNewFileName(request.getFilename());
                request.setFilename(newName);
            } else {
                downloadResponse.setMessage("");
                return downloadResponse;
            }
        }
        process = new DownloadProcess(request);
        iProcess = (IDownloadProcess) Proxy.newProxyInstance(process.getClass().getClassLoader(), process.getClass().getInterfaces(),
                new TransitionInvocationHandler<IDownloadProcess, StateEnum, TransitionEnum>(process));
        process.setProxy(iProcess);
        iProcess.prepare();
        StateChangeListenerHub.INSTANCE.registerListener(this);
        iProcess.start();
        downloadResponse = new CreateTaskResponse();
        downloadResponse.setMessage("You task is started.");
        return downloadResponse;
    }

    private String getNewFileName(String filename) {
        String newName = null;
        List<DownloadTask> tasks = MetaManager.load("./meta");
        newName = generateNewName(filename, tasks);
        return newName;
    }

    private String generateNewName(String oldName, List<DownloadTask> tasks) {
        String newName = null;
        boolean match = false;
        for ( DownloadTask task : tasks ) {
            if ( oldName.equalsIgnoreCase(task.getFileName()) ) {
                match = true;
            }
        }
        if ( match ) {
            String[] results = oldName.split("\\.");
            if ( results.length == 1 ) {
                newName = results.toString().concat("1");
            } else if ( results.length == 2 ) {
                newName = results[0].concat(".").concat("1").concat(".").concat(results[1]);
            } else if ( results.length == 3 ) {
                int number = Integer.valueOf(results[1]).intValue();
                int newNumber = number + 1;
                newName = results[0].concat(".").concat(newNumber + "").concat(".").concat(results[2]);
            } else {
                int length = results.length;
                // TODO
                //
            }
            newName = generateNewName(newName, tasks);
        } else {
            newName = oldName;
        }
        return newName;
    }

    private boolean checkWhetherDownloaded(CreateTaskRequest request) {
        boolean result = false;
        List<DownloadTask> tasks = MetaManager.load("./meta");
        for ( DownloadTask task : tasks ) {
            if ( request.getUrl().equalsIgnoreCase(task.getUrl().toString()) ) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void onStateChanged(StateContext<?, ?> context) {
        final Object reactiveObject = context.getReactiveObject();
        if ( !( reactiveObject instanceof IDownloadProcess ) ) {
            return;
        }
        IDownloadProcess other = (IDownloadProcess) reactiveObject;
        if ( !other.getUrl().equals(this.iProcess.getUrl()) ) {
            return;
        }
        synchronized (process) {
            if ( context.getTransition() != TransitionEnum.Receive ) {
                return;
            }
            if ( process.getReceiveBytes() == process.getTask().getTotalLength() ) {
                StateChangeListenerHub.INSTANCE.removeListener(this);
                iProcess.finish();
            }
        }
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }
}
