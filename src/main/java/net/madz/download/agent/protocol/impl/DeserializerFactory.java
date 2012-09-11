package net.madz.download.agent.protocol.impl;

import net.madz.download.agent.protocol.IRequestDeserializer;

public class DeserializerFactory {

	public static IRequestDeserializer getInstance(String command) {
		if (command.equalsIgnoreCase(Commands.command.Help.name())) {
			return new HelpDeserializer();
		}else if (command.equalsIgnoreCase(Commands.command.Echo.name())) {
			return new EchoDeserializer();
		}
		return null;
	}

}
