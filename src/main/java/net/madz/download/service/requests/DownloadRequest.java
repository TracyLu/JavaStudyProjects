package net.madz.download.service.requests;
import java.net.URL;

import net.madz.download.service.IServiceRequest;


public final class DownloadRequest implements IServiceRequest {
	private String commandType;
	private URL url;
	private URL referencedUrl;
	private String folder;
	private String filename;
	private int threadNumber;

	public String getCommandType() {
		return commandType;
	}

	public void setCommandType(String commandType) {
		this.commandType = commandType;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public URL getReferencedUrl() {
		return referencedUrl;
	}

	public void setReferencedUrl(URL referencedUrl) {
		this.referencedUrl = referencedUrl;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

}
