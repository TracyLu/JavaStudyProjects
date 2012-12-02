package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;

public class PauseServiceResponse implements IServiceResponse {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
