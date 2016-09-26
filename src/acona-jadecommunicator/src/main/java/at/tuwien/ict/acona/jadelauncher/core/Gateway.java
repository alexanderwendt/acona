package at.tuwien.ict.acona.jadelauncher.core;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;

public interface Gateway {

	/**
	 * Send a message to an agent in an asynchron way
	 * 
	 * @param message
	 * @param receiver
	 * @throws Exception
	 */
	public void sendAsynchronousMessageToAgent(Message message) throws Exception;
	
	/**
	 * Send synchronous Message to Agent without timeout. Block until an answer is given.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public Message sendSynchronousMessageToAgent(Message message) throws Exception;
	
	/**
	 * Send synchronous Message to Agent with timeout. Block until an answer is given or timeout is passed. 
	 * 
	 * @param message
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Message sendSynchronousMessageToAgent(Message message, int timeout) throws Exception;
	
	/**
	 * @param agentName
	 * @param datapointName
	 * @throws Exception
	 */
	public void subscribeDatapoint(String agentName, String datapointName) throws Exception;
	
	/**
	 * @param agentName
	 * @param datapointName
	 * @throws Exception
	 */
	public void unsubscribeDatapoint(String agentName, String datapointName) throws Exception;
	
	/**
	 * Initialize jade gateway
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception;
	
	/**
	 * Shut down JADE gateway
	 * 
	 */
	public void shutDown();
	/**
	 * Add a component that listens for pushed messages from the agent system
	 */
	public void addListener(ListenerModule listener);
	
	/**
	 * @return
	 * @throws InterruptedException
	 */
	public Message getMessageFromAgent() throws InterruptedException;
	
	/**
	 * @param timeout
	 * @return
	 * @throws InterruptedException
	 */
	public Message getMessageFromAgent(long timeout) throws InterruptedException;
	
	/**
	 * @param timeout in ms
	 * @param ignoreEmptyValues if the result is empty strings, do nothing
	 * @return Datapoint
	 * @throws InterruptedException
	 */
	public Datapoint getDatapointFromAgent(long timeout, boolean ignoreEmptyValues) throws InterruptedException;
}