package at.tuwien.ict.kore.communicator.core;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.communicator.datastructurecontainer.BlackboardBean;
import at.tuwien.ict.kore.communicator.datastructurecontainer.CommunicationMode;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;

public class CommunicatorImpl extends Thread implements Communicator {
	private static Logger log = LoggerFactory.getLogger("main");
	
	private List<ListenerModule> listeners= new LinkedList<ListenerModule>();
	//private String messageFromAgent= "";
	private SynchronousQueue<String> blockingQueue = new SynchronousQueue<String>();
	
	/**
	 * Queue for all incoming messages
	 */
	private LinkedBlockingQueue<String> agentMessages = new LinkedBlockingQueue<String>(10);
	
	private String message="";
	
	
	/**
	 * Initialize gateway thread
	 */
	public void init() {
		try {
			log.info("Create gateway to a running jade environment");
			Properties p = new Properties();
			
			// -platform-id JadePlatform -local-host localhost -local-port 1099 -services jade.core.event.NotificationService

			p.setProperty(Profile.PLATFORM_ID,"JadePlatform");
			p.setProperty(Profile.MAIN_PORT, "1099");
			p.setProperty(Profile.MAIN_HOST,"localhost");
			p.setProperty(Profile.CONTAINER_NAME, "Gateway");
			p.setProperty(Profile.LOCAL_HOST,"localhost");
			p.setProperty(Profile.LOCAL_PORT, "1100");
			p.setProperty(Profile.SERVICES,"jade.core.event.NotificationService");
			
			Object[] args = new Object[1];
			args[0] = this.blockingQueue;
			JadeGateway.init("at.tuwien.ict.kore.communicator.core.BidirectionalGatewayAgent", args, p);
		
			log.info("Gateway active={}", JadeGateway.isGatewayActive());
			
			super.start();
		} catch (Exception e) {
			log.error("Cannot initialize gateway");
			throw e;
		}
	}
	
	public void run() {
		boolean isActive = true;
		
		//Create gateway
		//BlackboardBean board = new BlackboardBean();
		
		while (isActive==true) {
			try	{
				message = this.blockingQueue.take();
				this.agentMessages.put(message);
				this.updateListeners();
				log.info("Received message from agent={}", message);
			} catch(Exception e) { 
				log.error("Cannot receive message", e);
			}
		}
	}

	private void updateListeners() throws InterruptedException {
		if (this.listeners.isEmpty()==false) {
			String latestMessage = this.agentMessages.take();
			this.listeners.forEach((ListenerModule l)->l.updateValue(latestMessage));
		}
	}

	@Override
	public void sendAsynchronousMessageToAgent(String message, String receiver, String type) throws Exception {
		BlackboardBean board = new BlackboardBean();
		
		//synchronized (this.board) {
		board.setReceiver(receiver);
		board.setMessage(message);
		board.setType(type);
		JadeGateway.execute(board);
		//}
	}

	@Override
	public String sendSynchronousMessageToAgent(String message, String receiver, String messageType) throws Exception {
		String result = "";
		
		BlackboardBean board = new BlackboardBean();
		board.setCommunicationMode(CommunicationMode.SYNC);
		
		//synchronized (this.board) {
			//Construct the message
		board.setReceiver(receiver);
		board.setMessage(message);
		board.setType(messageType);
		JadeGateway.execute(board);
			
			//Get the result
		result = board.getMessage();
		//}
		
		return result;
	}

	@Override
	public String getMessageFromAgent() throws InterruptedException {
		return this.agentMessages.take();
	}
	
	@Override
	public String getMessageFromAgent(long timeout) throws InterruptedException {
		return this.agentMessages.poll(timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public void addListener(ListenerModule listener) {
		this.listeners.add(listener);
	}

	@Override
	public void shutDown() {
		log.info("Shutting down Jade gateway");
		JadeGateway.shutdown();
	}
}
