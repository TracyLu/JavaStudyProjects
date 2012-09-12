package net.madz.download.service;

import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.requests.HelpRequest;

@Command(name = "help", request = HelpRequest.class, arguments = { @Arg(name = "commandName") }, options = { }, description = "Display command usage.")
public class HelpService implements IService<HelpRequest> {

	private static ServiceHub serviceHub = ServiceHub.getInstance();
	
	public static HelpService getInstance (String commandName) {
		if (!commandName.equalsIgnoreCase(HelpService.class.getAnnotation(Command.class).name())) {
			
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
		request.getCommandName();
		return null;
	}

}
