package at.tuwien.ict.kore.communicator.core;

public interface Communicator {

	/**
	 * Send a message to an agent in an asynchron way
	 * 
	 * @param message
	 * @param receiver
	 * @throws Exception
	 */
	public void sendAsynchronousMessageToAgent(String message, String receiver, String messageType) throws Exception;
	public String sendSynchronousMessageToAgent(String message, String receiver, String messageType) throws Exception;
	public void init();
	public void shutDown();
	/**
	 * Add a component that listens for pushed messages from the agent system
	 */
	public void addListener(ListenerModule listener);
	public String getMessageFromAgent() throws InterruptedException;
	public String getMessageFromAgent(long timeout) throws InterruptedException;
}
