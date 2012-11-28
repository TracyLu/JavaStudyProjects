package net.madz.download.service.services;

import java.lang.reflect.Proxy;

import net.madz.core.lifecycle.IStateChangeListener;
import net.madz.core.lifecycle.StateContext;
import net.madz.core.lifecycle.impl.StateChangeListenerHub;
import net.madz.core.lifecycle.impl.TransitionInvocationHandler;
import net.madz.download.agent.ITelnetClient;
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
import net.madz.download.service.requests.ResumeTaskRequest;
import net.madz.download.service.responses.ResumeTaskResponse;

@Command(arguments = { @Arg(name = "taskName", description="taskName is the file name of the task.") }, commandName = "resume-task", options = {}, request = ResumeTaskRequest.class,
        description = "Resume a paused download task by specifing the task name.")
public class ResumeTaskService implements IService<ResumeTaskRequest>,IStateChangeListener {

    private DownloadProcess process;
    private IDownloadProcess iProcess;
    private ITelnetClient client;

    @Override
    public void start() {
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {
    }

    @Override
    public void setClient(ITelnetClient client) {
        this.client = client;
    }

    @Override
    public IServiceResponse processRequest(ResumeTaskRequest request) throws ServiceException {
        process = new DownloadProcess(request);
        iProcess = (IDownloadProcess) Proxy.newProxyInstance(process.getClass().getClassLoader(), process.getClass().getInterfaces(),
                new TransitionInvocationHandler<IDownloadProcess, StateEnum, TransitionEnum>(process));
        process.setProxy(iProcess);
        StateChangeListenerHub.INSTANCE.registerListener(this);
        iProcess.resume();
        iProcess.start();
        ResumeTaskResponse resumeResponse = new ResumeTaskResponse();
        resumeResponse.setMessage("You task is resumed to download.");
        return resumeResponse;
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
}
