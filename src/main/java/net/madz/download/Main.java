package net.madz.download;

import java.io.File;

import net.madz.download.connector.IServiceEndpoint;
import net.madz.download.connector.impl.ServiceEndpoint;
import net.madz.download.service.ServiceHub;

public class Main {

    public static void main(String[] args) {
        ServiceHub.getInstance();
        // Create meta folder when not exists
        //
        
        new File("./meta/new").mkdir();
        new File("./meta/downloading").mkdirs();
        new File("./meta/finished").mkdirs();
        new File("./meta/deleted").mkdirs();
        new File("./meta/paused").mkdirs();
        IServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.start();
    }
}
