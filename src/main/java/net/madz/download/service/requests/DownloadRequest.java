package net.madz.download.service.requests;

import java.net.MalformedURLException;
import java.net.URL;

import net.madz.download.service.IServiceRequest;

public final class DownloadRequest implements IServiceRequest {
	private URL url;
	private String folder;
	private String filename;
	private int threadNumber = 10;

	public URL getUrl() {
		return url;
	}

	public void setUrl(String url) throws MalformedURLException {
		this.url = new URL(url);
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
