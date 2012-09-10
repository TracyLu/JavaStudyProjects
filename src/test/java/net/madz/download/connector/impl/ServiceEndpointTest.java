package net.madz.download.connector.impl;

import static org.junit.Assert.fail;

import java.net.Socket;

import junit.framework.Assert;
import mockit.Mocked;
import net.madz.download.agent.ITelnetClient;
import net.madz.download.connector.IServiceEndpoint;

import org.junit.Test;

public class ServiceEndpointTest {

	private IServiceEndpoint prepareServiceEndpoint() {
		return null;
	}

	@Test(expected = NullPointerException.class)
	public void testCreateClient_sockect_is_null() {
		IServiceEndpoint endpoint = prepareServiceEndpoint();
		endpoint.start();
		try {
			endpoint.createClient(null);
			fail("Not expected to be here.");
		} catch (NullPointerException ex) {
			Assert.assertEquals("Socket should not be null.", ex.getMessage());
			throw ex;
		} finally {
			endpoint.stop();
		}
	}

	@Test
	public void testCreateClient_socket_is_not_null(@Mocked final Socket socket) {
		IServiceEndpoint endpoint = prepareServiceEndpoint();
		endpoint.start();
		ITelnetClient client = endpoint.createClient(socket);
		Assert.assertNotNull(client);
		client.start();
		Assert.assertEquals(true, client.isStarted());
		client.stop();
		endpoint.stop();
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateClient_endpoint_not_started(
			@Mocked final Socket socket) {
		IServiceEndpoint endpoint = prepareServiceEndpoint();
		try {
			endpoint.createClient(socket);
			fail("Not expected to be here.");
		} catch (IllegalStateException ex) {
			Assert.assertEquals("Service is not started yet.", ex.getMessage());
			throw ex;
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testStart_avoid_duplicate_start() {
		IServiceEndpoint endpoint = prepareServiceEndpoint();
		endpoint.start();
		Assert.assertEquals(true, endpoint.isStarted());
		try {
			endpoint.start();
			fail("Not expected to be here.");
		} catch (IllegalStateException ex) {
			Assert.assertEquals("Endpoint is already started.", ex.getMessage());
			throw ex;
		}
	}

	@Test
	public void testIsStarted() {
		IServiceEndpoint endpoint = prepareServiceEndpoint();
		Assert.assertEquals(false, endpoint.isStarted());
		endpoint.start();
		Assert.assertEquals(true, endpoint.isStarted());
	}

	@Test(expected = IllegalStateException.class)
	public void testStop_service_not_started() {
		IServiceEndpoint endpoint = prepareServiceEndpoint();
		try {
			endpoint.stop();
			fail("Not expected to be here.");
		} catch (IllegalStateException ex) {
			Assert.assertEquals("Endpoint is not started yet.", ex.getMessage());
			throw ex;
		}

	}

	@Test
	public void testStop_service_started() {
		IServiceEndpoint endpoint = prepareServiceEndpoint();
		endpoint.start();
		Assert.assertEquals(true, endpoint.isStarted());
		endpoint.stop();
		Assert.assertEquals(false, endpoint.isStarted());
	}

}
