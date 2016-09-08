package at.tuwien.ict.acona.jadelauncher.core;

import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.jadelauncher.datastructurecontainer.BlackboardBean;
import at.tuwien.ict.acona.jadelauncher.util.ACLUtils;
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
	
	private static Logger log = LoggerFactory.getLogger(BidirectionalGatewayAgent.class);
	private SynchronousQueue<Message> blockingQueue;
	BlackboardBean receiveBoard = null;
	
	//private String inReplyWith = ""; 
	
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
			
			//If sync, then no release command until message has been processed
			if (this.receiveBoard.isSyncronizedRequest()==false) {
				this.releaseCommand(receiveBoard);
			} else {
				msg.setConversationId(MessageInfo.SYNCREQUEST);
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
			//ACLMessage customTemplate = new ACLMessage(ACLMessage.REQUEST);
			//customTemplate.addUserDefinedParameter(Keys.MODE.toString(), AconaSync.ASYNCHRONIZED.toString());
			MessageTemplate template = MessageTemplate.MatchEncoding(AconaSync.ASYNCHRONIZED.toString());//.MatchCustom(customTemplate, true);
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
			log.debug("AID={}", this.myAgent.getAID().getName());
			MessageTemplate template = MessageTemplate.MatchEncoding(AconaSync.SYNCHRONIZED.toString());
			ACLMessage msg = receive(template);
			
			if ((msg!=null) && (receiveBoard!=null))	{				
				try {
					log.debug("Synchron message receival={}", msg);
					log.debug("Received from={}, message={}", msg.getSender().getLocalName(), msg.getContent());
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
