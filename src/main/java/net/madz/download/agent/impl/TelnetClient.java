package net.madz.download.agent.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import net.madz.download.LogUtils;
import net.madz.download.MessageConsts;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.agent.protocol.impl.RawCommand;
import net.madz.download.agent.protocol.impl.RequestDeserializer;
import net.madz.download.agent.protocol.impl.ResponseSerializer;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.exception.ErrorMessage;
import net.madz.download.service.requests.HelpRequest;

public class TelnetClient implements ITelnetClient {

	private final Socket socket;
	private final BufferedReader reader;
	private final PrintWriter writer;
	private Thread listeningThread;
	private boolean started = false;

	public TelnetClient(Socket socket) throws IOException {
		super();
		this.socket = socket;
		validateSocket();
		this.reader = new BufferedReader(new InputStreamReader(
				this.socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(
				socket.getOutputStream()));
	}

	private void validateSocket() {
		if (null == this.socket) {
			throw new NullPointerException(MessageConsts.SOCKET_CANNOT_BE_NULL);
		}
		if (!this.socket.isConnected()) {
			throw new IllegalStateException(
					MessageConsts.SOCKET_IS_NOT_CONNECTED);
		}
	}

	@Override
	public synchronized void start() {
		if (this.isStarted()) {
			throw new IllegalStateException(
					MessageConsts.SERVICE_IS_ALREADY_STARTED);
		}

		validatePrequesit();

		listeningThread = allocatListeningThread();
		listeningThread.start();
		while (!started) {
			try {
				wait();
			} catch (InterruptedException e) {
				LogUtils.error(TelnetClient.class, e);
			}
		}
	}

	private Thread allocatListeningThread() {
		return new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (TelnetClient.this) {
					started = true;
					TelnetClient.this.notify();
				}
				try {
					while (!Thread.currentThread().isInterrupted()) {
						RequestDeserializer deserializer = null;
						ResponseSerializer serializer = null;
						IService service = null;
						IServiceRequest serviceRequest;
						final IServiceResponse serviceResponse;
						final String plainTextResponse;

						final String plainTextRequest = reader.readLine();
						LogUtils.debug(TelnetClient.class, "Received request: "
								+ plainTextRequest);

						deserializer = new RequestDeserializer();
						serializer = new ResponseSerializer();
						IServiceRequest request = null;

						try {
							request = new RequestDeserializer()
									.unmarshall(plainTextRequest);
						} catch (IllegalStateException ex) {
							HelpRequest helpRequest = new HelpRequest();
							helpRequest.setCommandName("help");
							if (ErrorMessage.COMMAND_NOT_FOUND
									.equalsIgnoreCase(ex.getMessage())) {
								helpRequest.setArgCommandName("");
								request = helpRequest;
							} else {
								String commandName = deserializer.parseCommand(
										plainTextRequest).getName();
								if ("help".equalsIgnoreCase(commandName)) {
									helpRequest.setArgCommandName("");
								} else {
									helpRequest.setArgCommandName(commandName);
								}
								request = helpRequest;
							}
							service = ServiceHub.getService("help");
						} catch (ErrorException ex) {
							writer.println(ex.getMessage());
							writer.flush();
							continue;
						}
						if (null == service) {
							service = ServiceHub.getService(request
									.getCommandName());
						}

						serviceResponse = service.processRequest(request);
						plainTextResponse = serializer
								.marshall(serviceResponse);
						writer.println(plainTextResponse);
						writer.flush();
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

	private void validatePrequesit() {
		validateSocket();
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
	public synchronized void stop() {
		if (!this.isStarted()) {
			throw new IllegalStateException(
					MessageConsts.SERVICE_IS_NOT_STARTED_YET);
		}
		listeningThread.interrupt();
		try {
			while (isStarted()) {
				wait();
			}
		} catch (InterruptedException ignored) {
		}
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}

}
