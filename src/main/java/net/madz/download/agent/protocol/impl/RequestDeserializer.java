package net.madz.download.agent.protocol.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.madz.download.agent.impl.TelnetClient;
import net.madz.download.agent.protocol.IRequestDeserializer;
import net.madz.download.service.HelpService;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceRequest;
import net.madz.download.service.ServiceHub;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;

public class RequestDeserializer implements IRequestDeserializer {

	@Override
	public IServiceRequest unmarshall(String plainText) {
		RawCommand rawCommand = TelnetClient.parseCommand(plainText);
		IService<?> service = ServiceHub.getService(rawCommand.getName());
		if (null == service) {
			service = ServiceHub.getService(HelpService.class.getAnnotation(
					Command.class).name());
		}
		Command annotation = service.getClass().getAnnotation(Command.class);
		Class<? extends IServiceRequest> requestClass = annotation.request();
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
		Arg[] arguments = annotation.arguments();
		Option[] options = annotation.options();
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
}
