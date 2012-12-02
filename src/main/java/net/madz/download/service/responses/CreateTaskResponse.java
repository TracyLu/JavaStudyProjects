package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;

public class CreateTaskResponse implements IServiceResponse {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
