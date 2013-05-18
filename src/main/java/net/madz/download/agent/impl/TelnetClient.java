package net.madz.download.agent.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.agent.protocol.impl.RequestDeserializer;
import net.madz.download.agent.protocol.impl.ResponseSerializer;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.exception.ExceptionMessage;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.HelpRequest;
import net.madz.download.utils.LogUtils;
import net.madz.download.utils.MessageConsts;

public class TelnetClient<R extends IServiceRequest, S extends IService<R>> implements ITelnetClient {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private Thread listeningThread;
    private boolean started = false;

    public TelnetClient(Socket socket) throws IOException {
        super();
        this.socket = socket;
        validateSocket();
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public String acquireConfirm(String request) {
        if ( null == request || 0 >= request.length() ) {
            LogUtils.error(TelnetClient.class, new Exception("Please give correct feedback."));
        }
        writer.write(request);
        writer.flush();
        String response = null;
        try {
            response = reader.readLine();
            while ( null == response || 0 >= response.length() ) {
                writer.write(request);
                writer.flush();
                response = reader.readLine();
            }
        } catch (IOException e) {
            LogUtils.error(TelnetClient.class, e);
        }
        return response;
    }

    private Thread allocatListeningThread() {
        return new Thread(new Runnable() {

            private IServiceRequest handleCommandIllegal(RequestDeserializer deserializer, final String plainTextRequest, IllegalStateException ex) {
                IServiceRequest request;
                HelpRequest helpRequest = new HelpRequest();
                helpRequest.setCommandName("help");
                if ( ExceptionMessage.COMMAND_NOT_FOUND.equalsIgnoreCase(ex.getMessage()) ) {
                    helpRequest.setArgCommandName("");
                    request = helpRequest;
                } else {
                    String commandName = RequestDeserializer.parseCommand(plainTextRequest).getName();
                    if ( "help".equalsIgnoreCase(commandName) ) {
                        helpRequest.setArgCommandName("");
                    } else {
                        helpRequest.setArgCommandName(commandName);
                    }
                    request = helpRequest;
                }
                return request;
            }

            private void printAndFlush(String string) {
                writer.println(string);
                writer.flush();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                synchronized (TelnetClient.this) {
                    started = true;
                    TelnetClient.this.notify();
                }
                try {
                    while ( !Thread.currentThread().isInterrupted() ) {
                        final String plainTextRequest = reader.readLine();
                        final RequestDeserializer deserializer = new RequestDeserializer();
                        final ResponseSerializer serializer = new ResponseSerializer();
                        LogUtils.debug(TelnetClient.class, "Received request: " + plainTextRequest);
                        R request = null;
                        S service = null;
                        try {
                            request = (R) new RequestDeserializer().unmarshall(plainTextRequest);
                            service = (S) ServiceHub.getInstance().getService(request.getCommandName());
                        } catch (IllegalStateException ex) {
                            request = (R) handleCommandIllegal(deserializer, plainTextRequest, ex);
                            service = (S) ServiceHub.getInstance().getService("help");
                        } catch (ServiceException ex) {
                            printAndFlush(ex.getMessage());
                            continue;
                        }
                        IServiceResponse serviceResponse = null;
                        try {
                            service.setClient(TelnetClient.this);
                            serviceResponse = service.processRequest(request);
                            final String plainTextResponse = serializer.marshall(serviceResponse);
                            printAndFlush(plainTextResponse);
                        } catch (ServiceException ex) {
                            printAndFlush(ex.getMessage());
                            continue;
                        } finally {
                            if ( "quit".equalsIgnoreCase(request.getCommandName()) ) {
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    LogUtils.error(TelnetClient.class, e);
                } finally {
                    synchronized (TelnetClient.this) {
                        releaseAll();
                        started = false;
                        listeningThread = null;
                        TelnetClient.this.notify();
                    }
                }
            }
        });
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    /* package */void releaseAll() {
        try {
            reader.close();
        } catch (IOException ignored) {
        }
        writer.close();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public synchronized void start() {
        if ( this.isStarted() ) {
            throw new IllegalStateException(MessageConsts.SERVICE_IS_ALREADY_STARTED);
        }
        validatePrequesit();
        listeningThread = allocatListeningThread();
        listeningThread.setName("TelnetClient listening thread");
        listeningThread.start();
        while ( !started ) {
            try {
                wait();
            } catch (InterruptedException e) {
                LogUtils.error(TelnetClient.class, e);
            }
        }
    }

    @Override
    public synchronized void stop() {
        if ( !this.isStarted() ) {
            throw new IllegalStateException(MessageConsts.SERVICE_IS_NOT_STARTED_YET);
        }
        listeningThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while ( isStarted() ) {
                wait();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private void validatePrequesit() {
        validateSocket();
    }

    private void validateSocket() {
        if ( null == this.socket ) {
            throw new NullPointerException(MessageConsts.SOCKET_CANNOT_BE_NULL);
        }
        if ( !this.socket.isConnected() ) {
            throw new IllegalStateException(MessageConsts.SOCKET_IS_NOT_CONNECTED);
        }
    }
}
