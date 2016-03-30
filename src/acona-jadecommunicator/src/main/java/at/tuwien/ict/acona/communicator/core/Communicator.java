package at.tuwien.ict.acona.communicator.core;

import at.tuwien.ict.acona.cell.datastructures.Message;

public interface Communicator {

	/**
	 * Send a message to an agent in an asynchron way
	 * 
	 * @param message
	 * @param receiver
	 * @throws Exception
	 */
	public void sendAsynchronousMessageToAgent(Message message) throws Exception;
	//public void sendAsynchronousMessageToAgent(JsonObject object) throws Exception;
	public Message sendSynchronousMessageToAgent(Message message) throws Exception;
	public Message sendSynchronousMessageToAgent(Message message, int timeout) throws Exception;
	//public JsonObject sendSynchronousMessageToAgent(JsonObject object) throws Exception;
	public void init() throws Exception;
	public void shutDown();
	/**
	 * Add a component that listens for pushed messages from the agent system
	 */
	public void addListener(ListenerModule listener);
	public Message getMessageFromAgent() throws InterruptedException;
	public Message getMessageFromAgent(long timeout) throws InterruptedException;
}
