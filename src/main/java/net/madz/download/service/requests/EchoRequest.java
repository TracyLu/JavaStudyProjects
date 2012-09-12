package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;

public class EchoRequest implements IServiceRequest {

	private final String message;
	private boolean lowerCase;

	public EchoRequest(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public boolean isLowerCase() {
		return lowerCase;
	}

	public void setLowerCase(boolean lowerCase) {
		this.lowerCase = lowerCase;
	}

}
