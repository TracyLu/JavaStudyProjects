package net.madz.download.engine.impl.metadata;

import org.junit.Test;

public class IdFactoryTest {

    @Test
    public void testGetId() {
        for ( int i = 0; i < 100; i++ ) {
            System.out.println(IdFactory.getInstance().getId());
        }
    }
}
