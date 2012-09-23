package net.madz.download.connector;

import static org.junit.Assert.fail;

import java.net.Socket;

import junit.framework.Assert;
import mockit.Mocked;
import net.madz.download.AbsLifeCycleTest;
import net.madz.download.agent.ITelnetClient;

import org.junit.Test;

public abstract class AbsServiceEndpointTest<E extends IServiceEndpoint> extends AbsLifeCycleTest<E> {

    @Test(expected = NullPointerException.class)
    public void testCreateClient_sockect_is_null() {
        E endpoint = createService();
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
        E endpoint = createService();
        endpoint.start();
        ITelnetClient client = endpoint.createClient(socket);
        Assert.assertNotNull(client);
        client.start();
        Assert.assertEquals(true, client.isStarted());
        client.stop();
        endpoint.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateClient_endpoint_not_started(@Mocked final Socket socket) {
        E endpoint = createService();
        try {
            endpoint.createClient(socket);
            fail("Not expected to be here.");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Service is not started yet.", ex.getMessage());
            throw ex;
        }
    }
}
