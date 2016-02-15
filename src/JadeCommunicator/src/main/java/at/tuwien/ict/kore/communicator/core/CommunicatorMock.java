package at.tuwien.ict.kore.communicator.core;

public class CommunicatorMock implements Communicator {

	@Override
	public void sendAsynchronousMessageToAgent(String message, String receiver) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String sendSynchronousMessageToAgent(String message, String receiver) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
	public String getMessageFromAgent() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessageFromAgent(long timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		return "";
	}

}
