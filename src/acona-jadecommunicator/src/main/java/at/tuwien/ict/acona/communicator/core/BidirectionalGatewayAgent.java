package at.tuwien.ict.acona.communicator.core;

import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.communicator.datastructurecontainer.BlackboardBean;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
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
	private SynchronousQueue<Message> blockingQueue;
	BlackboardBean receiveBoard = null;
	
	private String inReplyWith = ""; 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void setup()	{
		log.info("Starting gateway agent={}", this.getLocalName());
		//Get arguments
		Object[] args = this.getArguments();
		if (args!=null && args[0] instanceof SynchronousQueue) {
			this.blockingQueue = (SynchronousQueue<Message>) args[0];
		}
		
		//Start gson
		gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Waiting for the answer
		addBehaviour(new ReceiveAsynchronousBehaviour(this.blockingQueue));
		addBehaviour(new ReceiveReply());
		
		super.setup();
	}
	
	/* (non-Javadoc)
	 * @see jade.wrapper.gateway.GatewayAgent#processCommand(java.lang.Object)
	 */
	protected void processCommand(Object obj) {
		//This method is called by the gateway to transfer a message to an agent
		
		if (obj instanceof BlackboardBean)	{
			receiveBoard = (BlackboardBean)obj;
			log.info("Command received={}", receiveBoard);
			
			//Set the receiver and send the command to the receiver
			ACLMessage msg = ACLUtils.convertToACL(receiveBoard.getMessage());
			
					
//			new ACLMessage(ACLMessage.REQUEST);
//			msg.addReceiver(new AID(receiveBoard.getMessage().get(Message.RECEIVER).getAsString(), AID.ISLOCALNAME));
//			msg.setConversationId(CONVERSATIONID);
//			msg.setInReplyTo(JsonMessage.CONVERSATIONIDREQUEST);
			
//			String content = receiveBoard.getMessageBodyAsString();
//			msg.setContent(content);
//			msg.setOntology(this.receiveBoard.getMessage().get(Message.TYPE).getAsString());	//Ontology used as type
			
			
			receiveBoard.setMessage(Message.newMessage()); //JsonMessage.createMessage(JsonMessage.toContentString("ACK"), "", ""));
			
			//If sync, then no release command until message has been processed
			if (this.receiveBoard.isSyncronizedRequest()==false) {
				this.releaseCommand(receiveBoard);
			} else {
				msg.setConversationId(MessageInfo.SYNCREQUEST);
				//msg.setReplyWith(JsonMessage.CONVERSATIONIDREQUEST);
				//this.inReplyWith = msg.getConversationId();
			}
			
			send(msg);
		}
	}
	
	public class ReceiveAsynchronousBehaviour extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		private SynchronousQueue<Message> commMessageQueue = null;
		
		public ReceiveAsynchronousBehaviour(SynchronousQueue<Message> comm) {
			this.commMessageQueue = comm;
		}
		
		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.not(MessageTemplate.MatchConversationId(MessageInfo.SYNCREQUEST));
			ACLMessage msg = receive(template);
			//ACLMessage msg = receive();
			//log.debug("Asynchron message receival={}", msg);
			
			if (msg!=null)	{
				log.debug("Asynchron message receival={}", msg);
				log.debug("Received from={}, message={}", msg.getSender().getLocalName(), msg.getContent());
				
				try {
					//Create JsonObject from Message
					Message message = ACLUtils.convertToMessage(msg);
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
			//MessageTemplate template = MessageTemplate.MatchReplyWith(JsonMessage.CONVERSATIONIDREQUEST);
			log.debug("AID={}", this.myAgent.getAID().getName());
			MessageTemplate template = MessageTemplate.MatchConversationId(MessageInfo.SYNCREQUEST);
			ACLMessage msg = receive(template);
			//ACLMessage msg = receive();
			
			if ((msg!=null) && (receiveBoard!=null))	{				
				try {
					receiveBoard.setMessage(ACLUtils.convertToMessage(msg));	
				} catch (Exception e) {
					log.error("Cannot convert message to JSON={}. Abort operation", msg, e);
				}
				
				releaseCommand(receiveBoard);
			} else {
				//log.warn("Message not supposed as reply for this agent={}", msg);
				block();
			}
			
		}
		
	}
}
