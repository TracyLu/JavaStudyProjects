package net.madz.download.agent.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import net.madz.download.LogUtils;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.agent.protocol.IRequestDeserializer;
import net.madz.download.agent.protocol.IResponseSerializer;
import net.madz.download.agent.protocol.impl.Commands;
import net.madz.download.agent.protocol.impl.DeserializerFactory;
import net.madz.download.agent.protocol.impl.SerializerFactory;
import net.madz.download.agent.protocol.impl.ServiceFactory;
import net.madz.download.agent.protocol.impl.Commands.command;
import net.madz.download.service.HelpService;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;

public class TelnetClient implements ITelnetClient {

	static final String SERVICE_IS_ALREADY_STARTED = "Service is already started.";
	static final String SERVICE_IS_NOT_STARTED_YET = "Service is not started or stopped.";
	static final String DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED = "Deserializer, serializer, service should be initialized.";
	static final String SOCKET_CANNOT_BE_NULL = "Socket cannot be null.";
	static final String SOCKET_IS_NOT_CONNECTED = "Socket is not connected.";

	private final Socket socket;
	private final BufferedReader reader;
	private final PrintWriter writer;
	private Thread listeningThread;
	private boolean started = false;

	// to be injected
	private IRequestDeserializer deserializer;
	private IResponseSerializer serializer;
	private IService service;

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
			throw new NullPointerException(SOCKET_CANNOT_BE_NULL);
		}
		if (!this.socket.isConnected()) {
			throw new IllegalStateException(SOCKET_IS_NOT_CONNECTED);
		}
	}

	@Override
	public void setDeserializer(IRequestDeserializer deserializer) {
		this.deserializer = deserializer;
	}

	@Override
	public void setSerializer(IResponseSerializer serializer) {
		this.serializer = serializer;
	}

	@Override
	public void setService(IService service) {
		this.service = service;
	}

	@Override
	public synchronized void start() {
		if (this.isStarted()) {
			throw new IllegalStateException(SERVICE_IS_ALREADY_STARTED);
		}

		// validatePrequesit();

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
						final String plainTextRequest = reader.readLine();
						LogUtils.debug(TelnetClient.class, "Received request: "
								+ plainTextRequest);
						
						
						
						// Analyze the command
						String commandName = parseCommand(plainTextRequest);
						
						deserializer = DeserializerFactory
								.getInstance(commandName);
						service = ServiceHub.getService(commandName);
						Class<? extends IService> serviceClassObj = service.getClass();
						Command command = serviceClassObj.getAnnotation(Command.class);
						boolean satisfied = checkCommand(plainTextRequest, command);
						serializer = SerializerFactory
								.getInstance(commandName);

						final IServiceRequest serviceRequest = deserializer
								.unmarshall(plainTextRequest);
						final IServiceResponse serviceResponse = service
								.processRequest(serviceRequest);
						final String plainTextResponse = serializer
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

			private boolean checkCommand(String plainTextRequest,
					Command command) {
				Option[] options = command.options();
				Arg[] arguments = command.arguments();
				System.out.println("=====");
				return false;
			}

		});
	}

	private String parseCommand(String plainTextRequest) {
		if (null == plainTextRequest || 0 >= plainTextRequest.length()) {
			throw new NullPointerException("Please input command.");
		}
		String[] split = plainTextRequest.split("\\s");
		for (String item : split) {
			System.out.println(item);
		}
		if (split.length <= 1) {
			throw new IllegalStateException(
					"Please use correct command syntax. example: iget help version");
		}
		return split[1];
	}

	private void validatePrequesit() {
		if (null == deserializer || null == serializer || null == service) {
			throw new IllegalStateException(
					DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED);
		}
		if (!service.isStarted()) {
			throw new IllegalStateException(SERVICE_IS_NOT_STARTED_YET);
		}
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
			throw new IllegalStateException(SERVICE_IS_NOT_STARTED_YET);
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
