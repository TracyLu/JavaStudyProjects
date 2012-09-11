package net.madz.download.agent.protocol.impl;

import net.madz.download.agent.protocol.IResponseSerializer;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.responses.HelpResponse;

public class HelpSerializer implements IResponseSerializer {

	@Override
	public String marshall(IServiceResponse response) {
		return response.toString();
	}

}
