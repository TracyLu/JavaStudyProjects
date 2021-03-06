package net.madz.download.service.requests;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.madz.download.service.IServiceRequest;
import net.madz.download.service.exception.ExceptionMessage;
import net.madz.download.service.exception.ServiceException;

public final class CreateTaskRequest implements IServiceRequest {

    private String commandName;
    private String url;
    private String referURL = "http://www.baidu.com"; // make a faked refer URL
    private String folder;
    private String filename;
    private int threadNumber = 10;
    private boolean reCreate = false;

    public String getCommandName() {
        return commandName;
    }

    public String getFilename() {
        return filename;
    }

    public String getFolder() {
        return folder;
    }

    public String getReferURL() {
        return referURL;
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public String getUrl() {
        return url;
    }

    public boolean isReCreate() {
        return reCreate;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setReCreate(boolean reCreate) {
        this.reCreate = reCreate;
    }

    public void setReferURL(String referURL) {
        this.referURL = referURL;
    }
    
    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void validate() throws ServiceException {
        // validate url
        //
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new ServiceException(ExceptionMessage.URL_NOT_AVAILABLE);
        }
        // validate folder
        //
        File file = new File(folder);
        if ( !file.exists() ) {
            throw new ServiceException(ExceptionMessage.FOLDER_NOT_EXISTS);
        }
        // validate filename
        // ignored for the moment
        //
        
        // validate thread number
        //
        if (this.threadNumber <= 0 || threadNumber > 20) {
            throw new ServiceException(ExceptionMessage.THREAD_NUMBER_ILLEGAL);
        }
    }
}
