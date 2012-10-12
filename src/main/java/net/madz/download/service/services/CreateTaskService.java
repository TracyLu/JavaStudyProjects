package net.madz.download.service.services;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.madz.core.lifecycle.impl.TransitionInvocationHandler;
import net.madz.download.LogUtils;
import net.madz.download.engine.DownloadProcess;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.responses.CreateTaskResponse;

@Command(arguments = { @Arg(name = "url", description = "the address of remote file."), @Arg(name = "folder", description = "where to store the file"),
        @Arg(name = "filename", description = "new file name.") }, commandName = "create-task", options = { @Option(description = "thread number",
        fullName = "--threadNumber", shortName = "-n") }, request = CreateTaskRequest.class,
        description = "This command is responsible for downloding specified url resource.")
public class CreateTaskService implements IService<CreateTaskRequest> {

    private ExecutorService pool; // We use thread pool
    private Lock poolLock = new ReentrantLock();
    private Condition allDone = poolLock.newCondition();
    private int doneNumber = 0;
    private static File file; // Point to the storage file
    private File logFile;
    private DownloadTask task;

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
        final DownloadProcess process = new DownloadProcess(request);
        IDownloadProcess iProcess = (IDownloadProcess) Proxy.newProxyInstance(process.getClass().getClassLoader(), process.getClass().getInterfaces(),
                new TransitionInvocationHandler(process));
        iProcess.prepare();
        iProcess.start();
        synchronized (DownloadProcess.class) {
            try {
                if (process.getReceiveBytes() != process.getTask().getTotalLength() ) {
                    DownloadProcess.class.wait();
                }
                iProcess.finish();
            } catch (InterruptedException ignored) {
                LogUtils.error(CreateTaskService.class, ignored);
            }
        }
        CreateTaskResponse downloadResponse = new CreateTaskResponse();
        downloadResponse.setMessage("You task is done.");
        return downloadResponse;
    }
}
