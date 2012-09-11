package net.madz.download.service;

import net.madz.download.agent.protocol.impl.Commands;
import net.madz.download.service.requests.EchoRequest;
import net.madz.download.service.requests.HelpRequest;
import net.madz.download.service.responses.EchoResponse;
import net.madz.download.service.responses.HelpResponse;

public class HelpService implements IService {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public IServiceResponse processRequest(IServiceRequest request) {
		if (request instanceof HelpRequest) {
			HelpResponse helpResponse = new HelpResponse();
			helpResponse.setCommandName(((HelpRequest) request).getCommandName());
			String description = Commands.getDescription(((HelpRequest) request).getCommandName());
			helpResponse.setDescription(description);
			return helpResponse;
		} else {
			return null;
		}
	}

}
