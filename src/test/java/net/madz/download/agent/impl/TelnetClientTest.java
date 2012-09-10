package net.madz.download.agent.impl;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import junit.framework.Assert;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.agent.protocol.IRequestDeserializer;
import net.madz.download.agent.protocol.IResponseSerializer;
import net.madz.download.service.IService;

import org.junit.Test;

public class TelnetClientTest {

	@Test(expected = NullPointerException.class)
	public void testTelnetClient() {
		try {
			new TelnetClient(null);
		} catch (IOException e) {
			fail("IOException is not expected");
		} catch (NullPointerException e) {
			Assert.assertEquals(TelnetClient.SOCKET_CANNOT_BE_NULL,
					e.getMessage());
			throw e;
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testStart_socket_not_connected(@Mocked final Socket socket) {

		new NonStrictExpectations() {
			{
				try {
					socket.isConnected();
					result = false;

					socket.getInputStream();
					result = new ByteArrayInputStream(new byte[1024]);

					socket.getOutputStream();
					result = new ByteArrayOutputStream();

				} catch (IOException e) {
					fail("IOException is not expected");
				}
			}
		};

		try {
			final ITelnetClient client = new TelnetClient(socket);
			startClient(client);
		} catch (IOException e) {
			fail("IOException is not expected");
		} catch (IllegalStateException e) {
			Assert.assertEquals(TelnetClient.SOCKET_IS_NOT_CONNECTED,
					e.getMessage());
			throw e;
		}

	}

	@Test(expected = IllegalStateException.class)
	public void testStart_deserializer_not_initialized(
			@Mocked final Socket socket,
			@Mocked final IResponseSerializer serializer,
			@Mocked final IService service) {
		prepareResourceForPositiveCase(socket, service);

		try {
			TelnetClient client = new TelnetClient(socket);
			client.setSerializer(serializer);
			client.setService(service);
			client.setDeserializer(null);
			startClient(client);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			Assert.assertEquals(
					TelnetClient.DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED,
					ex.getMessage());
			throw ex;
		}
		fail("Not expected here");
	}

	private void prepareResourceForPositiveCase(final Socket socket,
			final IService service) {
		new NonStrictExpectations() {
			{
				try {
					socket.isConnected();
					result = true;

					socket.getInputStream();
					result = new ByteArrayInputStream(new byte[1024]);

					socket.getOutputStream();
					result = new ByteArrayOutputStream();

					service.isStarted();
					result = true;
				} catch (IOException e) {
					fail("IOException is not expected");
				}
			}
		};
	}

	@Test(expected = IllegalStateException.class)
	public void testStart_serializer_not_initialized(
			@Mocked final Socket socket,
			@Mocked final IRequestDeserializer deserializer,
			@Mocked final IService service) {
		prepareResourceForPositiveCase(socket, service);

		try {
			TelnetClient client = new TelnetClient(socket);
			client.setDeserializer(deserializer);
			client.setService(service);
			client.setSerializer(null);
			startClient(client);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			Assert.assertEquals(
					TelnetClient.DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED,
					ex.getMessage());
			throw ex;
		}
		fail("Not expected here.");
	}

	@Test
	public void testStart_avoid_duplicate_start(@Mocked final Socket socket,
			@Mocked final IRequestDeserializer deserializer,
			@Mocked final IResponseSerializer serializer,
			@Mocked final IService service) {
		prepareResourceForPositiveCase(socket, service);

		ITelnetClient client = initClient(socket, deserializer, serializer,
				service);
		startClient(client);
		try {
			startClient(client);
			fail("Not expected to be here.");
		} catch (IllegalStateException ex) {
			Assert.assertEquals(TelnetClient.SERVICE_IS_ALREADY_STARTED,
					ex.getMessage());
		}
	}

	@Test
	public void testStop(@Mocked final Socket socket,
			@Mocked final IRequestDeserializer deserializer,
			@Mocked final IResponseSerializer serializer,
			@Mocked final IService service) {
		prepareResourceForPositiveCase(socket, service);
		ITelnetClient client = initClient(socket, deserializer, serializer,
				service);
		startClient(client);
		client.stop();

		Assert.assertEquals(false, client.isStarted());
		try {
			client.stop();
			fail("Not expected to be here");
		} catch (IllegalStateException ex) {
			Assert.assertEquals(TelnetClient.SERVICE_IS_NOT_STARTED_YET,
					ex.getMessage());
		}

	}

	private ITelnetClient initClient(final Socket socket,
			final IRequestDeserializer deserializer,
			final IResponseSerializer serializer, final IService service) {
		TelnetClient client = null;
		try {
			client = new TelnetClient(socket);
			client.setDeserializer(deserializer);
			client.setService(service);
			client.setSerializer(serializer);
		} catch (IOException e) {
			fail("IOException is not expected");
		}
		return client;
	}

	private void startClient(ITelnetClient client) {
		client.start();
	}

	@Test
	public void testIsStarted(@Mocked final Socket socket,
			@Mocked final IRequestDeserializer deserializer,
			@Mocked final IResponseSerializer serializer,
			@Mocked final IService service) {
		prepareResourceForPositiveCase(socket, service);
		ITelnetClient client = initClient(socket, deserializer, serializer,
				service);
		Assert.assertEquals(false, client.isStarted());
		startClient(client);
		Assert.assertEquals(true, client.isStarted());
	}

}
