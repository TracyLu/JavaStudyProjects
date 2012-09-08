package net.madz.download.agent.protocol;

import net.madz.download.service.IServiceResponse;

public interface IResponseSerializer {

	String marshall(IServiceResponse response);
}
