package at.tuwien.ict.acona.mq.cell.communication;

import java.util.List;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunction;
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

	public void init(String host, String userName, String password, CellFunction function) throws Exception;

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

	// public void addRequestHandlerFunction(String topicSuffix, Function<Request, Response> function) throws Exception;

	// public void removeRequestHandlerFunction(String topicSuffix) throws Exception;

	/**
	 * Shut down communicator of the agent
	 */
	public void shutDown();

	/**
	 * Read datapoints with wildcards from the storage of an agent
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public List<Datapoint> readWildcard(String address) throws Exception;

	/**
	 * Read a specific datapoint from the storage of an agent
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public Datapoint read(String address) throws Exception;

	/**
	 * Write a datapoint to the data storage of an agent
	 * 
	 * @param datapoint
	 * @throws Exception
	 */
	public void write(Datapoint datapoint) throws Exception;

	/**
	 * Subscribe any datapoint through MQTT
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public Datapoint subscribeDatapoint(String address) throws Exception;

	/**
	 * Unsubscribe any datapoint through MQTT
	 * 
	 * @param address
	 * @throws Exception
	 */
	public void unsubscribeDatapoint(String address) throws Exception;

	public void publishDatapoint(Datapoint dp) throws Exception;

	/**
	 * Publish a topic with any general json element
	 * 
	 * @param topic
	 * @param message
	 * @throws Exception
	 */
	public void publishTopic(String topic, JsonElement message) throws Exception;

	/**
	 * Subscribe a topic with the topic filter of MQTT
	 * 
	 * @param topicfilter
	 * @throws Exception
	 */
	public void subscribeTopic(String topicfilter) throws Exception;

	/**
	 * Unsubscribes a MQTT topic
	 * 
	 * @param topicfilter
	 * @throws Exception
	 */
	public void unsubscribeTopic(String topicfilter) throws Exception;
}
