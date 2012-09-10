package net.madz.download;

import net.madz.download.connector.IServiceEndpoint;
import net.madz.download.connector.impl.ServiceEndpoint;

public class Main {
	public static void main(String[] args) {
		IServiceEndpoint endpoint = new ServiceEndpoint();
		endpoint.start();
	}
}
