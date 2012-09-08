package net.madz.download.agent.protocol;

import net.madz.download.service.IServiceRequest;

public interface IRequestDeserializer {

	IServiceRequest unmarshall(String plainText);
}
