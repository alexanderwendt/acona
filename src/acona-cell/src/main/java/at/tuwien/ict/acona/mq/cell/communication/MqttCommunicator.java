package at.tuwien.ict.acona.mq.cell.communication;

import java.util.function.Function;

import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * The basic class for communication between functions, services in or within other agents
 * 
 * @author wendt
 *
 */
public interface MqttCommunicator {

//	private final String host = "tcp://127.0.0.1:1883";
//	private final String username = "acona";
//	private final String password = "acona";
//	private final String functionName = "FunctionRequester";
//	private final String agentName = "agent1";

	public void init(String host, String userName, String password, String agentName, String functionName) throws Exception;

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
	public Response execute(String agentNameAndService, Request methodParameters, int timeout) throws Exception;

	/**
	 * Execute a service, where the whole address is written in one string, i.e. agent:service
	 * 
	 * @param agentNameAndService
	 * @param methodParameters
	 * @return
	 * @throws Exception
	 */
	public Response execute(String agentNameAndService, Request methodParameters) throws Exception;

	/**
	 * Execute the service as a non-blocking function
	 * 
	 * @param agentName
	 * @param serviceName
	 * @param methodParameters
	 * @throws Exception
	 */
	public void executeAsynchronous(String agentAndServiceName, Request methodParameters) throws Exception;

	public void addRequestHandlerFunction(String topicSuffix, Function<Request, Response> function) throws Exception;

	public void removeRequestHandlerFunction(String topicSuffix) throws Exception;

	/**
	 * Shut down communicator of the agent
	 */
	public void shutDown();

	public Datapoint read(String topic) throws Exception;

	public void write(Datapoint datapoint) throws Exception;

	public Datapoint subscribeDatapoint(String key) throws Exception;

	public void unsubscribeDatapoint(String key) throws Exception;

}
