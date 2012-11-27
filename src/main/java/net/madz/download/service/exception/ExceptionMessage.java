package net.madz.download.service.exception;

public class ExceptionMessage {

    public static final String COMMAND_NOT_FOUND = "Command could not be found.";
    public static final String COMMAND_OPTION_ILLEGAL = "Command option is illegal.";
    public static final String COMMAND_ARGUMENT_ILLEGAL = "Command argument is illegal.";
    public static final String URL_NOT_AVAILABLE = "URL is not available.";
    public static final String FOLDER_NOT_EXISTS = "The target folder does not exist.";
    public static final String INNER_ERROR = "Inner error occured, please contact Administrator.";
    public static final String LOG_FILE_WAS_NOT_FOUND = "Log file was not found, please check under folder meta/downloading. Log file name is: ";
    public static final String LOG_FILE_IS_NOT_COMPLETE = "Log file is not complete, please contact administrator. Log file name is: ";
    public static final String THREAD_NUMBER_ILLEGAL = "Pleas input correct thread number, it should be less than 20, and greater than 0.";
}
