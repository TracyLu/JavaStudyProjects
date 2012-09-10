package net.madz.download.agent.protocol.impl;

import net.madz.download.agent.protocol.IRequestDeserializer;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.requests.EchoRequest;

public class EchoDeserializer implements IRequestDeserializer {

	@Override
	public IServiceRequest unmarshall(String plainText) {
		return new EchoRequest(plainText);
	}

}
