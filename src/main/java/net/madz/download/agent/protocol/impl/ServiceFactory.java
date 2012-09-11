package net.madz.download.agent.protocol.impl;

import net.madz.download.service.EchoService;
import net.madz.download.service.HelpService;
import net.madz.download.service.IService;

public class ServiceFactory {

	public static IService getInstance(String commandName) {
		if (commandName.equalsIgnoreCase(Commands.command.Help.name())) {
			return new HelpService();
		}
		if (commandName.equalsIgnoreCase(Commands.command.Echo.name())) {
			return new EchoService();
		}
		return null;
	}

}
