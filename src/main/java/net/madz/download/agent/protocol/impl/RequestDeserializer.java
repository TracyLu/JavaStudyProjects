package net.madz.download.agent.protocol.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.exception.ErrorMessage;
import net.madz.download.service.requests.HelpRequest;
import net.madz.download.service.services.HelpService;

public class RequestDeserializer {
	private boolean satisfied;

	public boolean isSatisfied() {
		return satisfied;
	}

	public IServiceRequest unmarshall(String plainText) throws ErrorException {
		// Step 1: Analyze the command string and generate RawCommand
		//
		RawCommand rawCommand = RequestDeserializer.parseCommand(plainText);

		IService<?> service = ServiceHub.getService(rawCommand.getName());
		if (null == service) {
			throw new IllegalStateException(ErrorMessage.COMMAND_NOT_FOUND);
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
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// For not satisfied, there are 2 scenarios:
		// 1. random characters, wrong or null
		// 2. correct command, but wrong arguments or options
		//
		if (!satisfied) {
			HelpRequest helpRequest = new HelpRequest();
			if (!(service instanceof HelpService)) {
				helpRequest.setCommandName("help");
				helpRequest.setArgCommandName(rawCommand.getName());
			} else {
				helpRequest.setCommandName("help");
				helpRequest.setArgCommandName("");
			}
			return helpRequest;
		}

		Arg[] arguments = command.arguments();
		Option[] options = command.options();

		try {
			requestClass.getMethod("setCommandName", String.class).invoke(
					serviceRequest, rawCommand.getName());
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < arguments.length; i++) {
			try {
				String methodName = "set" + normalized(arguments[i].name());
				Method method = requestClass
						.getMethod(methodName, String.class);
				String argValue = rawCommand.getArgs().get(i);
				method.invoke(serviceRequest, argValue);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		for (int j = 0; j < rawCommand.getOptions().size(); j++) {
			try {
				String option = rawCommand.getOptions().get(j);
				Method method = requestClass.getMethod("set"
						+ normalized(option), boolean.class);
				method.invoke(serviceRequest, true);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			serviceRequest.validate();
		} catch (ErrorException ex) {
			throw ex;
		}

		return serviceRequest;
	}

	private String normalized(String name) {
		if (null == name || 0 >= name.length()) {
			throw new NullPointerException("name should not be null.");
		}
		if (name.startsWith("-")) {
			name = name.replace("-", "");
		}
		String[] results = name.split("");
		if (0 <= results.length) {
			results[1] = results[1].toUpperCase();
		}
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < results.length; i++) {
			result.append(results[i]);
		}
		return result.toString();
	}

	public static RawCommand parseCommand(String plainTextRequest) {
		RawCommand command = new RawCommand();
		if (null == plainTextRequest || 0 >= plainTextRequest.length()) {
			command.setName("help");
			return command;
		}
		String[] results = plainTextRequest.split("\\s+");
		if (results.length <= 0) {
			command.setName("help");
			return command;
		}
		IService<?> service = ServiceHub.getService(results[0]);
		if (null == service) {
			command.setName("help");
			return command;
		}

		command.setName(results[0]);
		for (int i = 1; i < results.length; i++) {
			if (results[i].startsWith("-") || results[i].startsWith("--")) {
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
		for (String item : rawCommand.getOptions()) {
			boolean contained = false;
			for (Option expected : options) {
				if (item.equalsIgnoreCase(expected.fullName())
						|| expected.shortName().equalsIgnoreCase(item)) {
					contained = true;
				}
			}
			if (contained == false) {
				throw new IllegalStateException(
						ErrorMessage.COMMAND_OPTION_ILLEGAL);
			}
		}
		if (arguments.length != rawCommand.getArgs().size()) {
			throw new IllegalStateException(
					ErrorMessage.COMMAND_ARGUMENT_ILLEGAL);
		}
		return true;
	}
}
