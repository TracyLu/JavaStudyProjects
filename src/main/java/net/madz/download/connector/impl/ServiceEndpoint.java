package net.madz.download.connector.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.agent.impl.TelnetClient;
import net.madz.download.connector.IServiceEndpoint;
import net.madz.download.utils.LogUtils;
import net.madz.download.utils.MessageConsts;

public class ServiceEndpoint implements IServiceEndpoint {

    private boolean started = false;
    private Thread workingThread = null;
    private static ServiceEndpoint endpoint = new ServiceEndpoint();
    private ServerSocket server;

    private ServiceEndpoint() {
        super();
    }

    public static ServiceEndpoint getInstance() {
        return endpoint;
    }

    @Override
    public synchronized void start() {
        if ( isStarted() ) {
            throw new IllegalStateException(MessageConsts.SERVICE_IS_ALREADY_STARTED);
        }
        workingThread = new Thread(new Runnable() {


            @Override
            public void run() {
                synchronized (ServiceEndpoint.this) {
                    started = true;
                    ServiceEndpoint.this.notify();
                }
                server = null;
                try {
                    server = new ServerSocket(9999);
                    while ( !Thread.currentThread().isInterrupted() ) {
                        Socket socket = server.accept();
                        ITelnetClient client = createClient(socket);
                        client.start();
                    }
                } catch (IOException ignore) {
                    LogUtils.error(ServiceEndpoint.class, ignore);
                } finally {
                    synchronized (ServiceEndpoint.this) {
                        started = false;
                        try {
                            server.close();
                        } catch (IOException ignored) {
                            LogUtils.error(ServiceEndpoint.class, ignored);
                        }
                        workingThread = null;
                        ServiceEndpoint.this.notify();
                    }
                }
            }
        });
        workingThread.setName("ServiceEndpint working thread");
        workingThread.start();
        try {
            while ( !isStarted() ) {
                wait();
            }
        } catch (InterruptedException ignored) {
            LogUtils.error(ServiceEndpoint.class, ignored);
        }
    }

    @Override
    public synchronized boolean isStarted() {
        return started;
    }

    @Override
    public synchronized void stop() {
        if ( !isStarted() ) {
            throw new IllegalStateException(MessageConsts.SERVICE_IS_NOT_STARTED);
        }
        assert null != workingThread;
        workingThread.interrupt();
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while ( isStarted() ) {
                wait(500);
            }
        } catch (InterruptedException ignored) {
            LogUtils.error(ServiceEndpoint.class, ignored);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ITelnetClient createClient(Socket socket) {
        ITelnetClient client = null;
        try {
            client = new TelnetClient(socket);
        } catch (IOException ignored) {
            LogUtils.error(ServiceEndpoint.class, ignored);
        }
        return client;
    }
}
