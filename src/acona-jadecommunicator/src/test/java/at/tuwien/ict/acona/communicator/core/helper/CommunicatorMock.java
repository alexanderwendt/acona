package at.tuwien.ict.acona.communicator.core.helper;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.communicator.core.Communicator;
import at.tuwien.ict.acona.communicator.core.ListenerModule;

public class CommunicatorMock implements Communicator {

	@Override
	public void sendAsynchronousMessageToAgent(Message message) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message sendSynchronousMessageToAgent(Message message) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message sendSynchronousMessageToAgent(Message message, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() throws Exception {
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
	public Message getMessageFromAgent() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message getMessageFromAgent(long timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribeDatapoint(String agentName, String datapointName) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsubscribeDatapoint(String agentName, String datapointName) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Datapoint getDatapointFromAgent(long timeout, boolean ignoreEmptyValues) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
