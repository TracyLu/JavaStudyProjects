package net.madz.download.service;

import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.requests.DownloadRequest;

@Command(arguments = {
		@Arg(name = "url", description = "the address of remote file."),
		@Arg(name = "folder", description = "where to store the file"),
		@Arg(name = "filename", description = "new file name.") }, name = "create-task", options = { @Option(description = "thread number", fullName = "--threadNumber", shortName = "-n") }, request = DownloadRequest.class, description="")
public class CreateTaskService implements IService<DownloadRequest> {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public IServiceResponse processRequest(DownloadRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
