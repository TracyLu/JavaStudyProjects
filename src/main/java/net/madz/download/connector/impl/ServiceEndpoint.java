package net.madz.download.connector.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.madz.download.LogUtils;
import net.madz.download.MessageConsts;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.agent.impl.TelnetClient;
import net.madz.download.agent.protocol.impl.EchoDeserializer;
import net.madz.download.agent.protocol.impl.EchoSerializer;
import net.madz.download.connector.IServiceEndpoint;
import net.madz.download.service.EchoService;

public class ServiceEndpoint implements IServiceEndpoint {

	private final EchoService service;

	private boolean started = false;
	private Thread workingThread = null;

	public ServiceEndpoint() {
		super();
		this.service = new EchoService();
	}

	@Override
	public synchronized void start() {
		if (isStarted()) {
			throw new IllegalStateException(
					MessageConsts.SERVICE_IS_ALREADY_STARTED);
		}
		workingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (ServiceEndpoint.this) {
					started = true;
					ServiceEndpoint.this.notify();
				}
				try {
					ServerSocket server = new ServerSocket(9999);
					while (!Thread.currentThread().isInterrupted()) {
						Socket socket = server.accept();
						ITelnetClient client = createClient(socket);
						client.start();
					}
				} catch (IOException ignore) {
					LogUtils.error(ServiceEndpoint.class, ignore);
				} finally {
					synchronized (ServiceEndpoint.this) {
						started = false;
						workingThread = null;
						ServiceEndpoint.this.notify();
					}
				}
			}
		});
		workingThread.start();
		try {
			while (!isStarted()) {
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
		if (!isStarted()) {
			throw new IllegalStateException(
					MessageConsts.SERVICE_IS_NOT_STARTED);
		}
		assert null != workingThread;

		workingThread.interrupt();
		try {
			while (isStarted()) {
				wait();
			}
		} catch (InterruptedException ignored) {
			LogUtils.error(ServiceEndpoint.class, ignored);
		}

	}

	@Override
	public ITelnetClient createClient(Socket socket) {
		ITelnetClient client = null;
		try {
			client = new TelnetClient(socket);
			client.setDeserializer(new EchoDeserializer());
			client.setSerializer(new EchoSerializer());
			client.setService(service);
		} catch (IOException ignored) {
			LogUtils.error(ServiceEndpoint.class, ignored);
		}
		return client;
	}

}
