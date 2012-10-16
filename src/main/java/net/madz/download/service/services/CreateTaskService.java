package net.madz.download.service.services;

import java.lang.reflect.Proxy;

import net.madz.core.lifecycle.IStateChangeListener;
import net.madz.core.lifecycle.StateContext;
import net.madz.core.lifecycle.impl.StateChangeListenerHub;
import net.madz.core.lifecycle.impl.TransitionInvocationHandler;
import net.madz.download.engine.DownloadProcess;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.IDownloadProcess.TransitionEnum;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.responses.CreateTaskResponse;

@Command(arguments = { @Arg(name = "url", description = "the address of remote file."), @Arg(name = "folder", description = "where to store the file"),
        @Arg(name = "filename", description = "new file name.") }, commandName = "create-task", options = { @Option(description = "thread number",
        fullName = "--threadNumber", shortName = "-n") }, request = CreateTaskRequest.class,
        description = "This command is responsible for downloding specified url resource.")
public class CreateTaskService implements IService<CreateTaskRequest>, IStateChangeListener {

    private DownloadProcess process;
    private IDownloadProcess iProcess;

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
    public IServiceResponse processRequest(CreateTaskRequest request) throws ErrorException {
        process = new DownloadProcess(request);
        iProcess = (IDownloadProcess) Proxy.newProxyInstance(process.getClass().getClassLoader(), process.getClass().getInterfaces(),
                new TransitionInvocationHandler(process));
        process.setProxy(iProcess);
        iProcess.prepare();
        StateChangeListenerHub.INSTANCE.registerListener(this);
        iProcess.start();
        CreateTaskResponse downloadResponse = new CreateTaskResponse();
        downloadResponse.setMessage("You task is started.");
        return downloadResponse;
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
            if (context.getTransition() != TransitionEnum.Receive) {
                return;
            }
            if ( process.getReceiveBytes() == process.getTask().getTotalLength() ) {
                StateChangeListenerHub.INSTANCE.removeListener(this);                
                iProcess.finish();
            }
        }
    }
}
