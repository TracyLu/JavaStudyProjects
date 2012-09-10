package net.madz.download.connector.impl;

import java.net.Socket;

import net.madz.download.agent.ITelnetClient;
import net.madz.download.connector.IServiceEndpoint;

public class ServiceEndpoint implements IServiceEndpoint {

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ITelnetClient createClient(Socket socket) {
		// TODO Auto-generated method stub
		return null;
	}

}
