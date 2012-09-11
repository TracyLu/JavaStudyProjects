package net.madz.download.agent.protocol.impl;

public class Commands {
	public static enum command {
		Echo, Help, Version, Create_task, Delete_task, Update_task_configuration, View_task_configuration, List_task, Monitor_task
	}

	private static final String HELP = "Below are commands. For more detail, please use \"Iget help commandname\"."
			+ "\n"
			+ "1.Iget help"
			+ "\n"
			+ "2.Iget help commandName"
			+ "\n"
			+ "3.Iget version"
			+ "\n"
			+ "4.Iget create-task -url \"xx\" [-folder \"xx\"] [-filename \"xx\"] [-threadNumber xx]"
			+ "\n"
			+ "5.Iget delete-task -url \"xx\""
			+ "\n"
			+ "6.Iget delete-task -state \"xx\""
			+ "\n"
			+ "7.Iget update-task-configuration -url \"xx\" [-folder \"xx\"] [-filename \"xx\"] [-threadNumber xx]"
			+ "\n"
			+ "8.Iget view-task-configuration -url \"xx\""
			+ "\n"
			+ "9.Iget list-task [-all] [-state \"xx\"]"
			+ "\n"
			+ "10.Iget list-task -url \"xx\""
			+ "\n"
			+ "11.Iget monitor-task [-url \"xx\"] [-n yy]";
	private static final String VERSION = "VERSION";

	public static String getDescription(String commandName) {
		if (commandName.equalsIgnoreCase(command.Help.name())) {
			return HELP;
		} else if (commandName.equalsIgnoreCase(command.Version.name())) {
			return VERSION;
		}
		return "";
	}

}
