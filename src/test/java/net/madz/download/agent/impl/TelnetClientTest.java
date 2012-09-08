package net.madz.download.agent.impl;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import junit.framework.Assert;
import mockit.Mocked;
import mockit.NonStrictExpectations;
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
			final TelnetClient client = new TelnetClient(socket);
			client.start();
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
			@Mocked final Socket socket, @Mocked final IResponseSerializer serializer, @Mocked final IService service) {
		new NonStrictExpectations() {
			{
				try {
					socket.isConnected();
					result = true;

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
			TelnetClient client = new TelnetClient(socket);
			client.setSerializer(serializer);
			client.setService(service);
			client.setDeserializer(null);
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException ex) {
			Assert.assertEquals(TelnetClient.DESERIALIZER_SERIALIZER_SERVICE_SHOULD_BE_INITIALIZED, ex.getMessage());
			throw ex;
		}
		fail("Not expected here");
	}

	@Test(expected = IllegalStateException.class)
	public void testStart_serializr_not_initialized() {
		fail("Not yet implemented");
	}

	@Test(expected = IllegalStateException.class)
	public void testStart_avoid_duplicate_start(@Mocked final Socket socket) {
		fail("Not yet implemented");
	}

	@Test
	public void testStop() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsStarted() {
		fail("Not yet implemented");
	}

}
