package at.tuwien.ict.acona.cell.communicator;

import java.util.List;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;

public interface Communicator extends AgentCommunicator {
	/**
	 * Read a datapoint list
	 * 
	 * @param datapoints
	 * @return
	 * @throws Exception
	 */
	public List<Datapoint> read(List<String> datapoints) throws Exception;

	/**
	 * Read a datapoint list from another agent with timeout
	 * 
	 * @param datapoints
	 * @param agentName
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public List<Datapoint> read(String agentName, List<String> datapoints, int timeout) throws Exception;

	/**
	 * Read a datapoint
	 * 
	 * @param datapointName
	 * @return either a datapoint or an empty datapoint if nothing is found on that address.
	 * @throws Exception
	 */
	public Datapoint read(String datapointName) throws Exception;

	/**
	 * Read a range of datapoints like data.data.* to get the whole branch
	 * 
	 * @param datapointName
	 * @return
	 * @throws Exception
	 */
	public List<Datapoint> readWildcard(String datapointName) throws Exception;

	/**
	 * Read a datapoint from another agent
	 * 
	 * @param datapoint
	 * @param agentName
	 * @return
	 * @throws Exception
	 */
	public Datapoint read(String agentName, String datapoint) throws Exception;

	/**
	 * Read a datapoint from another agent with a timeout
	 * 
	 * @param datapoint
	 * @param agentName
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Datapoint read(String agentName, String datapoint, int timeout) throws Exception;

	/**
	 * Remove datapoint with address
	 * 
	 * @param datapointAddress
	 * @throws Exception
	 */
	public void remove(String datapointAddress) throws Exception;

	/**
	 * Remove a list of datapoints
	 * 
	 * @param datapoint
	 * @throws Exception
	 */
	public void remove(List<String> datapoint) throws Exception;

	/**
	 * Remove a list of datapoints for a specific agent
	 * 
	 * @param agentName
	 * @param datapoint
	 * @param timeout
	 * @throws Exception
	 */
	public void remove(String agentName, List<String> datapoint, int timeout) throws Exception;

	/**
	 * Write a list of datapoints to the local agent
	 * 
	 * @param datapoints
	 * @throws Exception
	 */
	public void write(List<Datapoint> datapoints) throws Exception;

	/**
	 * Write a list of datapoints to an agent with timeout and as a blocking method or not
	 * 
	 * @param datapoints
	 * @param agentName
	 * @param timeout
	 * @param blocking
	 * @throws Exception
	 */
	public void write(String agentName, List<Datapoint> datapoints, int timeout, boolean blocking) throws Exception;

	/**
	 * Write a single local datapoint to the own agent.
	 * 
	 * @param datapoint
	 * @throws Exception
	 */
	public void write(Datapoint datapoint) throws Exception;

	/**
	 * Write a single datapoint to an agent
	 * 
	 * @param datapoint
	 * @param agentName
	 * @throws Exception
	 */
	public void write(String agentName, Datapoint datapoint) throws Exception;

	/**
	 * Send a query and wait for an answer from the queried agent.
	 * 
	 * e.g. send START and get STATE back when a service has finished.
	 * 
	 * @param datapoint
	 *            to agent
	 * @param agentName
	 *            to agent
	 * @param timeout
	 *            to exit the query
	 * @return
	 * @throws Exception
	 */
	public Datapoint queryDatapoints(String writeAgentName, String writeAddress, JsonElement content, String resultAgentName, String resultAddress, JsonElement resultContent, int timeout) throws Exception;

	/**
	 * Query a datapoint, i.e. write content to an address and wait for an answer at another address. It can be in another agent as well.
	 * 
	 * @param writeAddress
	 * @param content
	 * @param writeAgentName
	 * @param resultAddress
	 * @param resultAgentName
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Datapoint queryDatapoints(String writeAgentName, String writeAddress, String content, String resultAgentName, String resultAddress, JsonElement resultContent, int timeout) throws Exception;

	/**
	 * Query a datapoint, i.e. write content to an address and wait for an answer at another address. It can be in another agent as well.
	 * 
	 * @param writeAddress
	 * @param content
	 * @param resultAddress
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String resultAddress, JsonElement resultContent, int timeout) throws Exception;

	/**
	 * Query a datapoint, i.e. write content to an address and wait for an answer at another address. It can be in another agent as well.
	 * 
	 * @param writeAddress
	 * @param content
	 * @param resultAddress
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Datapoint queryDatapoints(String writeAddress, String content, String resultAddress, String resultContent, int timeout) throws Exception;

	/**
	 * Execute a service in an agent and wait for a value in a certain datapoint
	 * 
	 * @param writeAgentName
	 * @param serviceName
	 * @param serviceParameter
	 * @param resultAgentName
	 * @param resultAddress
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Datapoint executeServiceQueryDatapoints(String writeAgentName, String serviceName, JsonRpcRequest serviceParameter, String resultAgentName, String resultAddress, JsonElement expectedResult, int timeout) throws Exception;

	/**
	 * Execute a cell function by setting the start command. Wait for the service to get service state = "FINIHSHED" and release the method. This Method is good to use if you don't have direct function
	 * arguments and has to wait until the function has finished.
	 * 
	 * @param agentAndService:
	 *            Set the [agent]:[service]. If only [service] is used, then the local agent is applied.
	 * @param timeout:
	 *            Set the timeout for waiting for the service to finish
	 * @return
	 */
	public ServiceState executeServiceBlocking(String agentAndService, int timeout);

	/**
	 * Execute a cell function by setting the start command. Wait for the service to get service state = "FINIHSHED" and release the method. This Method is good to use if you don't have direct function
	 * arguments and has to wait until the function has finished.
	 * 
	 * Default timeout is used.
	 * 
	 * @param agentAndService:
	 *            agentAndService: Set the [agent]:[service]. If only [service] is used, then the local agent is applied.
	 * @return
	 */
	public ServiceState executeServiceBlocking(String agentAndService);

	/**
	 * Subscribe a datapoint (also outside of the init function
	 * 
	 * @param agentid
	 * @param address
	 * @param callingCellfunctionName
	 * @return
	 * @throws Exception
	 */
	public Datapoint subscribeDatapoint(String key, String callingCellfunctionName) throws Exception;

	/**
	 * Unsubscribe a datapoint (also outside of the initfunction)
	 * 
	 * @param agentid
	 * @param address
	 * @param callingCellFunctionName
	 * @return
	 * @throws Exception
	 */
	public void unsubscribeDatapoint(String key, String callingCellFunctionName) throws Exception;

}
