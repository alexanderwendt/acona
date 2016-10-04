package at.tuwien.ict.acona.framework.interfaces;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.framework.modules.ServiceState;

public interface ControllerCellGateway {	//extends cellgateway
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	
	/**
	 * In cellconfig, set defaultservice=function1
	 * 
	 * @param serviceAndAgent
	 * @param command
	 * @return
	 */
	public ServiceState executeService(String service, JsonObject parameters);	//agent1:funktion=function.command in agent1, blocking, Kontroll, blocks until result from service
	
	public ServiceState executeService(String service, JsonObject parameters, int timeout);	//agent1:funktion=function.command in agent1, blocking, Kontroll, blocks until result from service
	
	public ServiceState executeService(String agent, String service, JsonObject parameters);	//agent1:funktion=function.command in agent1, blocking, Kontroll
	
	public ServiceState executeService(String agent, String service, JsonObject parameters, int timeout);	//agent1:funktion=function.command in agent1, blocking, Kontroll
	
	/**
	 * 
	 * 
	 * @param serviceAndAgent
	 * @param command
	 * @return
	 */
	
	public ServiceState queryState(String service);
	
	public ServiceState queryState(String service, int timeout);
	
	/**
	 * UC: USer provides a timeframe with default parameter
	 * 
	 * Topcontroller receives timeframe
	 * 
	 * @param service
	 * @param key
	 * @param object
	 */
	public void setProperty(String service, String key, String object);	//for the service
	public void setProperty(String key, String object);	//for the agent 
	
	
	
}
