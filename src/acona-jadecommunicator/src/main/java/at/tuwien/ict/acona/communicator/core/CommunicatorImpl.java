package at.tuwien.ict.acona.communicator.core;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.communicator.datastructurecontainer.BlackboardBean;
import at.tuwien.ict.acona.communicator.datastructurecontainer.CommunicationMode;
import jade.core.Profile;
import jade.util.leap.Properties;
import jade.wrapper.gateway.JadeGateway;

public class CommunicatorImpl extends Thread implements Communicator {
	private static Logger log = LoggerFactory.getLogger(CommunicatorImpl.class);
	
	private List<ListenerModule> listeners= new LinkedList<ListenerModule>();
	//private String messageFromAgent= "";
	private SynchronousQueue<Message> blockingQueue = new SynchronousQueue<Message>();
	
	private final static int TIMEOUT = 5000;
	
	/**
	 * Queue for all incoming messages
	 */
	private LinkedBlockingQueue<Message> agentMessages = new LinkedBlockingQueue<Message>(32);
	
	private Message message = Message.newMessage();
	
	
	/**
	 * Initialize gateway thread
	 * @throws Exception 
	 */
	public void init() throws Exception {
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
			JadeGateway.init("at.tuwien.ict.acona.communicator.core.BidirectionalGatewayAgent", args, p);
			
			//Init the agents and the gateway
			this.sendAsynchronousMessageToAgent(Message.newMessage().setReceiver("DF").setContent("init").setService(AconaServiceType.WRITE));
		
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
			Message latestMessage = this.agentMessages.take();
			this.listeners.forEach((ListenerModule l)->l.updateValue(latestMessage));
		}
	}
	
	@Override
	public void sendAsynchronousMessageToAgent(Message message) throws Exception {
		BlackboardBean board = new BlackboardBean();
		
		//synchronized (this.board) {
		board.setMessage(message);
		JadeGateway.execute(board);
		//}
	}

	@Override
	public Message sendSynchronousMessageToAgent(Message message) throws Exception {
		return this.sendSynchronousMessageToAgent(message, 0);
	}
	
	public Message sendSynchronousMessageToAgent(Message message, int timeout) throws Exception {
		Message result = null;
		
		BlackboardBean board = new BlackboardBean();		
		
		//Construct the message
		message.setMode(AconaSync.SYNCHRONIZED);
		board.setCommunicationMode(CommunicationMode.SYNC);
		board.setMessage(message);
		JadeGateway.execute(board, timeout);
			
		//Get the result
		result = board.getMessage();
		
		return result;
	}

	@Override
	public Message getMessageFromAgent() throws InterruptedException {
		return this.agentMessages.take();
	}
	
	@Override
	public Message getMessageFromAgent(long timeout) throws InterruptedException {
		Message result = null;
		try {
			result = this.agentMessages.poll(timeout, TimeUnit.MILLISECONDS);				
		} catch (InterruptedException e) {
			log.error("Interruption");
			throw e;
		}
		
		return result; 
	}
	
	@Override
	public Datapoint getDatapointFromAgent(long timeout, boolean ignoreEmptyValues) throws InterruptedException {
		Datapoint result = null;
		
		long startTime = System.currentTimeMillis();
		long endTime = startTime  + timeout;
		do {
			Message message = getMessageFromAgent(timeout);

			if (Datapoint.isDatapoint(message.getContent().getAsJsonObject())==true) {
				Datapoint datapoint = Datapoint.toDatapoint(message.getContent().getAsJsonObject());
				if (datapoint.getValue().toString().isEmpty()==false) {
					result = datapoint;
				}
			}
			
		} while (result.getValue().getAsString().isEmpty()==true && System.currentTimeMillis()<endTime && ignoreEmptyValues==true);	
		
		return result;
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

	@Override
	public void subscribeDatapoint(String agentName, String datapointName) throws Exception {
		this.sendSynchronousMessageToAgent(Message.newMessage().addReceiver(agentName)
				.setService(AconaServiceType.SUBSCRIBE)
				.setContent(Datapoint.newDatapoint(datapointName)));
	}

	@Override
	public void unsubscribeDatapoint(String agentName, String datapointName) throws Exception {
		this.sendSynchronousMessageToAgent(Message.newMessage().addReceiver(agentName)
				.setService(AconaServiceType.UNSUBSCRIBE)
				.setContent(Datapoint.newDatapoint(datapointName)));
		
	}
}
