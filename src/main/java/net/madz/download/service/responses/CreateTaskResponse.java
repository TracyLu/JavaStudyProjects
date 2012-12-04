package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;

public class CreateTaskResponse implements IServiceResponse {

    private final String id;

    public CreateTaskResponse(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
