package net.madz.download.service;

import net.madz.download.service.requests.EchoRequest;
import net.madz.download.service.responses.EchoResponse;

public class EchoService implements IService {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public IServiceResponse processRequest(IServiceRequest request) {
		if (request instanceof EchoRequest) {
			return new EchoResponse(((EchoRequest) request).getMessage());
		} else {
			return new EchoResponse(request.toString());
		}
	}

}
