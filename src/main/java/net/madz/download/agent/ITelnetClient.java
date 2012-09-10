package net.madz.download.agent;

import net.madz.download.ILifeCycle;
import net.madz.download.agent.protocol.IRequestDeserializer;
import net.madz.download.agent.protocol.IResponseSerializer;
import net.madz.download.service.IService;

public interface ITelnetClient extends ILifeCycle {

	void setService(IService service);

	void setSerializer(IResponseSerializer serializer);

	void setDeserializer(IRequestDeserializer deserializer);
	
}
