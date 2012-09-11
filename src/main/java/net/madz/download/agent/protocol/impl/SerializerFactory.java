package net.madz.download.agent.protocol.impl;

import net.madz.download.agent.protocol.IResponseSerializer;

public class SerializerFactory {

	public static IResponseSerializer getInstance(String commandName) {
		if (commandName.equalsIgnoreCase(Commands.command.Help.name())) {
			return new HelpSerializer();
		}
		if (commandName.equalsIgnoreCase(Commands.command.Echo.name())) {
			return new EchoSerializer();
		}
		return null;
	}

}
