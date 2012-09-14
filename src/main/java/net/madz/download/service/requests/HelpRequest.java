package net.madz.download.service.requests;

import net.madz.download.service.IServiceRequest;

public class HelpRequest implements IServiceRequest {
	private String commandName;

	public HelpRequest() {
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

}
