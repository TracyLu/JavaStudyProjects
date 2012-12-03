package net.madz.download.agent.protocol.impl;

import java.lang.reflect.Method;

import net.madz.download.LogUtils;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ExceptionMessage;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.requests.HelpRequest;
import net.madz.download.service.services.HelpService;

public class RequestDeserializer {

    private boolean satisfied;

    public boolean isSatisfied() {
        return satisfied;
    }

    public IServiceRequest unmarshall(String plainText) throws ServiceException {
        // Step 1: Analyze the command string and generate RawCommand
        //
        RawCommand rawCommand = RequestDeserializer.parseCommand(plainText);
        IService<?> service = ServiceHub.getInstance().getService(rawCommand.getName());
        if ( null == service ) {
            throw new IllegalStateException(ExceptionMessage.COMMAND_NOT_FOUND);
        }
        Command command = service.getClass().getAnnotation(Command.class);
        // Step 2: Validate raw command via Command annotation
        //
        satisfied = checkCommand(rawCommand, command);
        // Step 3: Generate request instance via reflect when satisfied = true.
        //
        Class<? extends IServiceRequest> requestClass = command.request();
        IServiceRequest serviceRequest = null;
        try {
            serviceRequest = requestClass.newInstance();
        } catch (Exception ex) {
            LogUtils.error(RequestDeserializer.class, ex);
            throw new ServiceException(ExceptionMessage.INNER_ERROR);
        }
        // For not satisfied, there are 2 scenarios:
        // 1. random characters, wrong or null
        // 2. correct command, but wrong arguments or options
        //
        if ( !satisfied ) {
            HelpRequest helpRequest = new HelpRequest();
            if ( !( service instanceof HelpService ) ) {
                helpRequest.setCommandName("help");
                helpRequest.setArgCommandName(rawCommand.getName());
            } else {
                helpRequest.setCommandName("help");
                helpRequest.setArgCommandName("");
            }
            return helpRequest;
        }
        Arg[] arguments = command.arguments();
        try {
            requestClass.getMethod("setCommandName", String.class).invoke(serviceRequest, rawCommand.getName());
        } catch (Exception ex) {
            LogUtils.error(RequestDeserializer.class, ex);
            throw new ServiceException(ExceptionMessage.INNER_ERROR);
        }
        for ( int i = 0; i < arguments.length; i++ ) {
            try {
                String methodName = "set" + normalized(arguments[i].name());
                Method method = requestClass.getMethod(methodName, String.class);
                String argValue = rawCommand.getArgs().get(i);
                method.invoke(serviceRequest, argValue);
            } catch (Exception ex) {
                LogUtils.error(RequestDeserializer.class, ex);
                throw new ServiceException(ExceptionMessage.INNER_ERROR);
            }
        }
        for ( int j = 0; j < rawCommand.getOptions().size(); j++ ) {
            try {
                String option = rawCommand.getOptions().get(j);
                Method method = requestClass.getMethod("set" + normalized(option), boolean.class);
                method.invoke(serviceRequest, true);
            } catch (Exception ex) {
                LogUtils.error(RequestDeserializer.class, ex);
                throw new ServiceException(ExceptionMessage.INNER_ERROR);
            }
        }
        try {
            serviceRequest.validate();
        } catch (ServiceException ex) {
            throw ex;
        }
        return serviceRequest;
    }

    private String normalized(String name) {
        if ( null == name || 0 >= name.length() ) {
            throw new NullPointerException("name should not be null.");
        }
        if ( name.startsWith("-") ) {
            name = name.replace("-", "");
        }
        String[] results = name.split("");
        if ( 0 <= results.length ) {
            results[1] = results[1].toUpperCase();
        }
        StringBuilder result = new StringBuilder();
        for ( int i = 0; i < results.length; i++ ) {
            result.append(results[i]);
        }
        return result.toString();
    }

    public static RawCommand parseCommand(String plainTextRequest) {
        RawCommand command = new RawCommand();
        if ( null == plainTextRequest || 0 >= plainTextRequest.length() ) {
            command.setName("help");
            return command;
        }
        String[] results = plainTextRequest.split("\\s+");
        if ( results.length <= 0 ) {
            command.setName("help");
            return command;
        }
        IService<?> service = ServiceHub.getInstance().getService(results[0]);
        if ( null == service ) {
            command.setName("help");
            return command;
        }
        command.setName(results[0]);
        for ( int i = 1; i < results.length; i++ ) {
            if ( results[i].startsWith("-") || results[i].startsWith("--") ) {
                command.addOption(results[i]);
            } else {
                command.addArg(results[i]);
            }
        }
        return command;
    }

    private boolean checkCommand(RawCommand rawCommand, Command command) {
        Option[] options = command.options();
        Arg[] arguments = command.arguments();
        for ( String item : rawCommand.getOptions() ) {
            boolean contained = false;
            for ( Option expected : options ) {
                if ( item.equalsIgnoreCase(expected.fullName()) || expected.shortName().equalsIgnoreCase(item) ) {
                    contained = true;
                }
            }
            if ( contained == false ) {
                throw new IllegalStateException(ExceptionMessage.COMMAND_OPTION_ILLEGAL);
            }
        }
        if ( arguments.length != rawCommand.getArgs().size() ) {
            throw new IllegalStateException(ExceptionMessage.COMMAND_ARGUMENT_ILLEGAL);
        }
        return true;
    }
}
