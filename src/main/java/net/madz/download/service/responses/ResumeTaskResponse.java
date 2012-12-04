package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;

public class ResumeTaskResponse implements IServiceResponse {

    private final String message;

    public ResumeTaskResponse(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ResumeTaskResponse [message=" + message + "]";
    }
}
