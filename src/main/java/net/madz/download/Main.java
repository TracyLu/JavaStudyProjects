package net.madz.download;

import net.madz.download.connector.impl.ServiceEndpoint;
import net.madz.download.engine.impl.DownloadEngine;
import net.madz.download.service.ServiceHub;

public class Main {

    public static void main(String[] args) {
        // Create and Start all services

        DownloadEngine.getInstance().start();
        
        ServiceHub.getInstance();
        
        ServiceEndpoint.getInstance().start();
    }
}
