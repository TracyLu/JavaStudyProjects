package net.madz.download.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.requests.HelpRequest;
import net.madz.download.service.responses.HelpResponse;

@Command(name = "help", request = HelpRequest.class, arguments = { @Arg(name = "commandName", description = "command name, short name or full name are all ok.") }, options = {}, description = "Display all the commands and simple description.")
public class HelpService implements IService<HelpRequest> {

	private static ServiceHub serviceHub = ServiceHub.getInstance();

	public static HelpService getInstance(String commandName) {
		if (!commandName.equalsIgnoreCase(HelpService.class.getAnnotation(
				Command.class).name())) {

		}
		return (HelpService) serviceHub.getService(commandName);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public IServiceResponse processRequest(HelpRequest request) {
		HelpResponse response = new HelpResponse();
		StringBuilder description = new StringBuilder();
		if (null == request.getCommandName()
				|| 0 >= request.getCommandName().length()) {
			iterateAllCommands(response, description);
		} else {
			IService<?> service = null;
			service = ServiceHub.getService(request.getCommandName());
			Command command = service.getClass().getAnnotation(Command.class);
			description.append("NAME");
			description.append("\n");
			description.append("\t" + command.name() + " - ");
			description.append(command.description());
			description.append("\n");
			if (command.options().length > 0) {
				description.append("\n");
				description.append("OPTIONS:");
				description.append("\n");
				for (Option option : command.options()) {
					description.append("\t");
					description.append(option.shortName());
					description.append(", ");
					description.append(option.fullName());
					description.append("\n");
					description.append("\t");
					description.append("    ");
					description.append(option.description());
					description.append("\n");
				}
			}
			if (command.arguments().length > 0) {
				description.append("\n");
				description.append("ARGUMENTS:");
				description.append("\n");
				for (Arg arg : command.arguments()) {
					description.append(arg.name());
					description.append("\t");
					description.append(arg.description());
					description.append("\n");
				}
			}
			response.setCommandName(request.getCommandName());
			response.setDescription(description.toString());
		}

		return response;
	}

	private void iterateAllCommands(HelpResponse response,
			StringBuilder description) {
		HashMap<String, IService> servicesregistry = ServiceHub
				.getServicesregistry();
		Iterator<Entry<String, IService>> iterator = servicesregistry
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, IService> next = iterator.next();
			String key = next.getKey();
			IService value = next.getValue();
			Command annotation = value.getClass().getAnnotation(Command.class);
			description.append(annotation.name());
			description.append("\t");
			description.append(annotation.description());
			description.append("\n");
		}
		response.setCommandName("Help");
		response.setDescription(description.toString());
	}

}
