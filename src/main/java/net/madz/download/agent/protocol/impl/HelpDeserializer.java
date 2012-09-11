package net.madz.download.agent.protocol.impl;

import net.madz.download.agent.protocol.IRequestDeserializer;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.requests.HelpRequest;

public class HelpDeserializer implements IRequestDeserializer {

	@Override
	public IServiceRequest unmarshall(String plainText) {
		String commandName = parseCommand(plainText);
		return new HelpRequest(commandName);
	}

	/***
	 * The commands follow below syntax could be parsed. 1. iget help or 2. iget
	 * help commandName For 1, all the commands usage will be shown. For 2,
	 * specified command usage will be shown.
	 * 
	 * @param plainText
	 * @return
	 */
	public static String parseCommand(String plainText) {
		if (null == plainText || 0 >= plainText.length()) {
			throw new NullPointerException("Please input command.");
		}
		String[] split = plainText.split("\\s");
		for (String item : split) {
			System.out.println(item);
		}
		if (split.length == 2 && split[1].equalsIgnoreCase(Commands.command.Help.name())) {
			return split[1];
		} 
		if (split.length == 3 && split[1].equalsIgnoreCase(Commands.command.Help.name())) {
			return split[2];
		}
		throw new IllegalStateException("Please use correct help command syntax. example: iget help version");
	}

}
