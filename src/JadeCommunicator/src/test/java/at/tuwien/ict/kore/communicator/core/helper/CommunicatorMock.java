package at.tuwien.ict.kore.communicator.core.helper;

import com.google.gson.JsonObject;

import at.tuwien.ict.kore.communicator.core.Communicator;
import at.tuwien.ict.kore.communicator.core.ListenerModule;

public class CommunicatorMock implements Communicator {

	@Override
	public void sendAsynchronousMessageToAgent(String message, String receiver, String type) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(ListenerModule listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject getMessageFromAgent() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject getMessageFromAgent(long timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		return new JsonObject();
	}

	@Override
	public void sendAsynchronousMessageToAgent(JsonObject object) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject sendSynchronousMessageToAgent(String messagebody, String receiver, String messageType)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject sendSynchronousMessageToAgent(JsonObject object) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
