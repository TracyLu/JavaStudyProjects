package net.madz.download;

import net.madz.download.connector.IServiceEndpoint;
import net.madz.download.connector.impl.ServiceEndpoint;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.metadata.MetaManager;

public class Main {

    public static void main(String[] args) {
        // Create metadata folders if not exists
        //
        MetaManager.initiateMetadataDirs();
        
        MetaManager.load("./meta");
        // Create and Start all services
        //
        ServiceHub.getInstance();
        
        IServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.start();
    }
}
