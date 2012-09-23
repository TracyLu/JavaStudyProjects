package net.madz.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.Test;

public abstract class AbsLifeCycleTest<T extends ILifeCycle> {

    protected abstract T createService();

    @Test(expected = IllegalStateException.class)
    public void testStart() {
        T service = createService();
        assertEquals(false, service.isStarted());
        service.start();
        assertEquals(true, service.isStarted());
        try {
            service.start();
        } catch (IllegalStateException ex) {
            assertEquals("Servie is already started.", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void testIsStarted() {
        T service = createService();
        Assert.assertEquals(false, service.isStarted());
        service.start();
        Assert.assertEquals(true, service.isStarted());
    }

    @Test(expected = IllegalStateException.class)
    public void testStop_service_not_started() {
        T service = createService();
        try {
            service.stop();
            fail("Not expected to be here.");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Servie is not started yet.", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void testStop_service_started() {
        T service = createService();
        service.start();
        Assert.assertEquals(true, service.isStarted());
        service.stop();
        Assert.assertEquals(false, service.isStarted());
    }
}
