package net.madz.download.service;

import net.madz.download.ILifeCycle;



public interface IService extends ILifeCycle {

	IServiceResponse processRequest(IServiceRequest request);

}
