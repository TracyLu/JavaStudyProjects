package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;

public class EchoResponse implements IServiceResponse {

	private final String echoMessage;

	public EchoResponse(String echoMessage) {
		super();
		this.echoMessage = echoMessage;
	}

	public String getEchoMessage() {
		return echoMessage;
	}

	@Override
	public String toString() {
		return "EchoResponse [echoMessage=" + echoMessage + "]";
	}

}
