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
import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.IServiceResponse;

public class TelnetClient implements ITelnetClient {

	static final String DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED = "deserializer, serializer, service should be initialized";
	static final String SOCKET_CANNOT_BE_NULL = "Socket cannot be null";
	static final String SOCKET_IS_NOT_CONNECTED = "Socket is not connected";

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

	public void setDeserializer(IRequestDeserializer deserializer) {
		this.deserializer = deserializer;
	}

	public void setSerializer(IResponseSerializer serializer) {
		this.serializer = serializer;
	}

	public void setService(IService service) {
		this.service = service;
	}

	@Override
	public synchronized void start() {

		if (null != listeningThread) {
			return;
		}

		validatePrequesit();

		listeningThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						final String plainTextRequest = reader.readLine();
						LogUtils.debug(TelnetClient.class, "Received request: "
								+ plainTextRequest);
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
					releaseAll();
					started = false;
				}

			}

		});
		started = true;
	}

	private void validatePrequesit() {
		if (null == deserializer || null == serializer || null == service) {
			throw new IllegalStateException(
					DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED);
		}
		if (!service.isStarted()) {
			throw new IllegalStateException(
					"service is not started or is stopped.");
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
		if (null == listeningThread) {
			return;
		}
		listeningThread.interrupt();
		listeningThread = null;
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}

}
