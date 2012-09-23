package net.madz.download.connector.impl;

import net.madz.download.connector.AbsServiceEndpointTest;

public class ServiceEndpointTest extends AbsServiceEndpointTest<ServiceEndpoint> {

    @Override
    public ServiceEndpoint createService() {
        return new ServiceEndpoint();
    }
}
