package net.madz.download.service;

import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.requests.EchoRequest;
import net.madz.download.service.responses.EchoResponse;


@Command(name = "echo", request = EchoRequest.class, arguments = { @Arg(name = "message") }, options = { @Option(shortName = "l", fullName = "lowerCase", description = "Echo service will return the lowerCase format message.") })
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
