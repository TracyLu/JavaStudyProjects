package net.madz.download.service;

import net.madz.download.ILifeCycle;



public interface IService extends ILifeCycle {

	/**
	 * No Exception can be thrown from this method.
	 * @param request
	 * @return
	 */
	IServiceResponse processRequest(IServiceRequest request);

}
