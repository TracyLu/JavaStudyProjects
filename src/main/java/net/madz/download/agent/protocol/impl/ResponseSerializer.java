package net.madz.download.agent.protocol.impl;

import net.madz.download.service.IServiceResponse;

public class ResponseSerializer {

    public String marshall(IServiceResponse response) {
        return response.toString();
    }
}
