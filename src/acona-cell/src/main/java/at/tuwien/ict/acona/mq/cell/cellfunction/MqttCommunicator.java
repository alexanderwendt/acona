package at.tuwien.ict.acona.mq.cell.cellfunction;

import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * The basic class for communication between functions, services in or within other agents
 * 
 * @author wendt
 *
 */
public interface MqttCommunicator {

	/**
	 * Sets default timeout for all communication functions
	 * 
	 * @param timeout
	 */
	public void setDefaultTimeout(int timeout);

	/**
	 * Get the default timeout
	 * 
	 * @return
	 */
	public int getDefaultTimeout();

	/**
	 * Execute a service
	 * 
	 * @param agentName:
	 *            Destination agent
	 * @param serviceName:
	 *            Desination service name
	 * @param methodParameters:
	 *            List of input for the service.
	 * @param timeout:
	 *            Timeout to wait
	 * @return
	 * @throws Exception
	 */
	public JsonRpcResponse execute(String agentNameAndService, JsonRpcRequest methodParameters, int timeout) throws Exception;

	/**
	 * Execute a service, where the whole address is written in one string, i.e. agent:service
	 * 
	 * @param agentNameAndService
	 * @param methodParameters
	 * @return
	 * @throws Exception
	 */
	public JsonRpcResponse execute(String agentNameAndService, JsonRpcRequest methodParameters) throws Exception;

	/**
	 * Execute the service as a non-blocking function
	 * 
	 * @param agentName
	 * @param serviceName
	 * @param methodParameters
	 * @throws Exception
	 */
	public void executeAsynchronous(String agentAndServiceName, JsonRpcRequest methodParameters) throws Exception;

	/**
	 * Shut down communicator of the agent
	 */
	public void shutDown();
}
