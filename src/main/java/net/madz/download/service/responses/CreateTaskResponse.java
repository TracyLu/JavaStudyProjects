package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;

public class CreateTaskResponse implements IServiceResponse {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DownloadResponse [message=" + message + "]";
    }
}
