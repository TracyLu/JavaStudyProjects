package net.madz.download;

import net.madz.download.connector.IServiceEndpoint;
import net.madz.download.connector.impl.ServiceEndpoint;
import net.madz.download.engine.impl.metadata.MetaManager;
import net.madz.download.service.ServiceHub;

public class Main {

    public static void main(String[] args) {
        // Create metadata folders if not exists
        //
        MetaManager.initiateMetadataDirs();
        // Create and Start all services
        //
        ServiceHub.getInstance();
        IServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.start();
    }
}
