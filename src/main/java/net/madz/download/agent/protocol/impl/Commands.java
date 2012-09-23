package net.madz.download.agent.protocol.impl;

public class Commands {

    public static enum command {
        Echo,
        Help,
        Version,
        Create_task,
        Delete_task,
        Update_task_configuration,
        View_task_configuration,
        List_task,
        Monitor_task
    }

    private static final String HELP = "Below are commands. For more detail, please use \"iget help commandname\"." + "\n" + "1.iget help" + "\n"
            + "2.iget help commandName" + "\n" + "3.iget version" + "\n"
            + "4.iget create-task -url \"xx\" [-folder \"xx\"] [-filename \"xx\"] [-threadNumber xx]" + "\n" + "5.iget delete-task -url \"xx\"" + "\n"
            + "6.iget delete-task -state \"xx\"" + "\n" + "7.iget update-task-configuration -url \"xx\" [-folder \"xx\"] [-filename \"xx\"] [-threadNumber xx]"
            + "\n" + "8.iget view-task-configuration -url \"xx\"" + "\n" + "9.iget list-task [-all] [-state \"xx\"]" + "\n" + "10.iget list-task -url \"xx\""
            + "\n" + "11.iget monitor-task [-url \"xx\"] [-n yy]";
    private static final String VERSION = "VERSION";

    public static String getDescription(String commandName) {
        if ( commandName.equalsIgnoreCase(command.Help.name()) ) {
            return HELP;
        } else if ( commandName.equalsIgnoreCase(command.Version.name()) ) {
            return VERSION;
        }
        return "";
    }
}
