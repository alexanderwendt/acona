package at.tuwien.ict.kore.communicator.core;

import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import at.tuwien.ict.kore.communicator.datastructurecontainer.BlackboardBean;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

/**
 * Create a gateway agent that receives a bean with data. This bean is then used to return data. 
 * 
 * @author wendt
 *
 */
public class BidirectionalGatewayAgent extends GatewayAgent {
	
	private static Logger log = LoggerFactory.getLogger("main");
	private Gson gson;
	private SynchronousQueue<JsonObject> blockingQueue;
	BlackboardBean receiveBoard = null;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/* (non-Javadoc)
	 * @see jade.wrapper.gateway.GatewayAgent#processCommand(java.lang.Object)
	 */
	protected void processCommand(Object obj) {
		//This method is called by the gateway to transfer a message to an agent
		
		if (obj instanceof BlackboardBean)	{
			receiveBoard = (BlackboardBean)obj;
			log.info("Command received={}", receiveBoard);
			
			//Set the receiver and send the command to the receiver
			ACLMessage msg = JsonMessage.convertToACL(receiveBoard.getMessage());
					
					
//			new ACLMessage(ACLMessage.REQUEST);
//			msg.addReceiver(new AID(receiveBoard.getMessage().get(Message.RECEIVER).getAsString(), AID.ISLOCALNAME));
//			//msg.setConversationId(CONVERSATIONID);
//			msg.setInReplyTo(Message.CONVERSATIONID);
//			String content = receiveBoard.getMessageBodyAsString();
//			msg.setContent(content);
//			msg.setOntology(this.receiveBoard.getMessage().get(Message.TYPE).getAsString());	//Ontology used as type
			
			receiveBoard.setMessage(JsonMessage.createMessage(JsonMessage.toContentString("ACK"), "", ""));
			
			//If sync, then no release command until message has been processed
			if (this.receiveBoard.isSyncronizedRequest()==false) {
				this.releaseCommand(receiveBoard);
			}
			
			send(msg);
		}
	}

	public void setup()	{
		log.info("Starting gateway agent={}", this.getLocalName());
		//Get arguments
		Object[] args = this.getArguments();
		if (args!=null && args[0] instanceof SynchronousQueue) {
			this.blockingQueue = (SynchronousQueue<JsonObject>) args[0];
		}
		
		//Start gson
		gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Waiting for the answer
		addBehaviour(new ReceiveAsynchronousBehaviour(this.blockingQueue));
		addBehaviour(new ReceiveReply());
		
		super.setup();
	}
	
	public class ReceiveAsynchronousBehaviour extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		private SynchronousQueue<JsonObject> commMessageQueue = null;
		
		public ReceiveAsynchronousBehaviour(SynchronousQueue<JsonObject> comm) {
			this.commMessageQueue = comm;
		}
		
		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.not(MessageTemplate.MatchReplyWith(JsonMessage.CONVERSATIONID));
			ACLMessage msg = receive(template);
			
			if (msg!=null)	{				
				log.debug("Received from={}, message={}", msg.getSender().getLocalName(), msg.getContent());
				
				try {
					//Create JsonObject from Message
					JsonObject message = JsonMessage.convertToJson(msg);
					this.commMessageQueue.add(message);
				} catch (Exception e) {
					log.error("Queue is full", e);
				}
				
				
			} else {
				block();
			}	
		}
	}
	
	public class ReceiveReply extends CyclicBehaviour {		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.MatchReplyWith(JsonMessage.CONVERSATIONID);
			ACLMessage msg = receive(template);
			
			if ((msg!=null) && (receiveBoard!=null))	{				
				receiveBoard.setMessage(gson.fromJson(msg.getContent(), JsonObject.class));
				releaseCommand(receiveBoard);				
			} else {
				block();
			}
			
		}
		
	}
}
