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
import net.madz.download.agent.protocol.IRequestDeserializer;
import net.madz.download.agent.protocol.IResponseSerializer;
import net.madz.download.agent.protocol.impl.RawCommand;
import net.madz.download.agent.protocol.impl.RequestDeserializer;
import net.madz.download.agent.protocol.impl.ResponseSerializer;
import net.madz.download.service.HelpService;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.requests.HelpRequest;

public class TelnetClient implements ITelnetClient {

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
			throw new NullPointerException(MessageConsts.SOCKET_CANNOT_BE_NULL);
		}
		if (!this.socket.isConnected()) {
			throw new IllegalStateException(
					MessageConsts.SOCKET_IS_NOT_CONNECTED);
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
			throw new IllegalStateException(
					MessageConsts.SERVICE_IS_ALREADY_STARTED);
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
						IServiceRequest serviceRequest;
						final IServiceResponse serviceResponse;
						final String plainTextResponse;

						final String plainTextRequest = reader.readLine();
						LogUtils.debug(TelnetClient.class, "Received request: "
								+ plainTextRequest);

						/**
						 * Code Review Comments:
						 * 
						 * There is a OO Design Principle, SRP, which can be referred from:
						 * http://en.wikipedia.org/wiki/Single_responsibility_principle
						 * 
						 * TelnetClient Class was initially designed for delegating communication 
						 * between Telnet Application and Download Service Process, which will including thread/process synchronization.
						 * 
						 * So the commandLine parsing, validating, constructing RawCommand and related stuff should be moved
						 * into RequestDeserializer Class. So that this class would be much more simplified.
						 */
						// Step 1: Analyze the command string and generate
						// RawCommand
						//
						RawCommand rawCommand = parseCommand(plainTextRequest);

						// Step 2: Validate raw command via Command annotation
						//
						service = ServiceHub.getService(rawCommand.getName());
						Class<? extends IService> serviceClassObj = service
								.getClass();
						Command command = serviceClassObj
								.getAnnotation(Command.class);
						boolean satisfied = checkCommand(rawCommand, command);

						deserializer = new RequestDeserializer();

						serializer = new ResponseSerializer();

						// For not satisfied, there are 2 scenarios:
						// 1. random characters, wrong or null
						// 2. correct command, but wrong arguments or options
						//
						if (!satisfied) {
							HelpRequest helpRequest = new HelpRequest();
							if (!(service instanceof HelpService)) {
								service = new HelpService();
								helpRequest.setCommandName(rawCommand.getName());
							} else {
								helpRequest.setCommandName("");
							}
							serviceRequest = helpRequest;
							serviceResponse = service
									.processRequest(serviceRequest);
							plainTextResponse = serviceResponse.toString();

							writer.println(plainTextResponse);
							writer.flush();

						} else {
							serviceRequest = deserializer
									.unmarshall(plainTextRequest);
							IServiceRequest request = new RequestDeserializer()
									.unmarshall(plainTextRequest);
							serviceResponse = service
									.processRequest(serviceRequest);
							plainTextResponse = serializer
									.marshall(serviceResponse);

							writer.println(plainTextResponse);
							writer.flush();
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

			private boolean checkCommand(RawCommand rawCommand, Command command) {
				Option[] options = command.options();
				Arg[] arguments = command.arguments();
				for (String item : rawCommand.getOptions()) {
					boolean contained = false;
					for (Option expected : options) {
						if (item.equalsIgnoreCase(expected.fullName())
								|| expected.shortName().equalsIgnoreCase(item)) {
							contained = true;
						}
					}
					if (contained == false) {
						return false;
					}
				}
				if (arguments.length != rawCommand.getArgs().size()) {
					return false;
				}
				return true;
			}

		});
	}

	public static RawCommand parseCommand(String plainTextRequest) {
		RawCommand command = new RawCommand();
		if (null == plainTextRequest || 0 >= plainTextRequest.length()) {
			command.setName("help");
			return command;
		}
		String[] results = plainTextRequest.split("\\s+");
		if (results.length <= 0) {
			command.setName("help");
			return command;
		}
		IService<?> service = ServiceHub.getService(results[0]);
		if (null == service) {
			command.setName("help");
			return command;
		}

		command.setName(results[0]);
		for (int i = 1; i < results.length; i++) {
			if (results[i].startsWith("-") || results[i].startsWith("--")) {
				command.addOption(results[i]);
			} else {
				command.addArg(results[i]);
			}
		}

		return command;
	}

	private void validatePrequesit() {
		if (null == deserializer || null == serializer || null == service) {
			throw new IllegalStateException(
					MessageConsts.DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED);
		}
		if (!service.isStarted()) {
			throw new IllegalStateException(
					MessageConsts.SERVICE_IS_NOT_STARTED_YET);
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
