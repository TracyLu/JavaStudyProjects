package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;

public class EchoRequest implements IServiceRequest {

	private final String message;

	public EchoRequest(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
