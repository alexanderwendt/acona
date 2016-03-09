package at.tuwien.ict.kore.communicator.core;

import com.google.gson.JsonObject;

public interface Communicator {

	/**
	 * Send a message to an agent in an asynchron way
	 * 
	 * @param message
	 * @param receiver
	 * @throws Exception
	 */
	public void sendAsynchronousMessageToAgent(String messagebody, String receiver, String messageType) throws Exception;
	public void sendAsynchronousMessageToAgent(JsonObject object) throws Exception;
	public JsonObject sendSynchronousMessageToAgent(String messagebody, String receiver, String messageType) throws Exception;
	public JsonObject sendSynchronousMessageToAgent(JsonObject object) throws Exception;
	public void init() throws Exception;
	public void shutDown();
	/**
	 * Add a component that listens for pushed messages from the agent system
	 */
	public void addListener(ListenerModule listener);
	public JsonObject getMessageFromAgent() throws InterruptedException;
	public JsonObject getMessageFromAgent(long timeout) throws InterruptedException;
}
