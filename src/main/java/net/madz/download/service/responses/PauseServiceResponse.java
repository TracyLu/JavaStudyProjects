package net.madz.download.service.responses;

import net.madz.download.service.IServiceResponse;


public class PauseServiceResponse implements IServiceResponse {
    private String url;

    
    public String getUrl() {
        return url;
    }

    
    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        return "PauseServiceResponse [url=" + url + "]";
    }
    
}
