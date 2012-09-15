package net.madz.download;

import net.madz.download.connector.IServiceEndpoint;
import net.madz.download.connector.impl.ServiceEndpoint;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.services.EchoService;

public class Main {
	public static void main(String[] args) {
		ServiceHub.getInstance();
		
		IServiceEndpoint endpoint = new ServiceEndpoint();
		endpoint.start();
	}
}
