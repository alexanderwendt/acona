package at.tuwien.ict.kore.communicator.core;

import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private final static String CONVERSATIONID = "externalRequest";
	
	private static Logger log = LoggerFactory.getLogger("main");
	private SynchronousQueue<String> blockingQueue;
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
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID(receiveBoard.getReceiver(), AID.ISLOCALNAME));
			//msg.setConversationId(CONVERSATIONID);
			msg.setInReplyTo(CONVERSATIONID);
			msg.setContent(receiveBoard.getMessage());			    
			
			receiveBoard.setMessage("ACK");
			
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
			this.blockingQueue = (SynchronousQueue<String>) args[0];
		}
		
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
		
		
		private SynchronousQueue<String> comm = null;
		
		public ReceiveAsynchronousBehaviour(SynchronousQueue<String> comm) {
			this.comm = comm;
		}
		
		@Override
		public void action() {
			MessageTemplate template = MessageTemplate.not(MessageTemplate.MatchReplyWith(CONVERSATIONID));
			ACLMessage msg = receive(template);
			
			if (msg!=null)	{				
				log.debug("Received from={}, message={}", msg.getSender().getLocalName(), msg.getContent());
				
				try {
					this.comm.add(msg.getContent());
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
			MessageTemplate template = MessageTemplate.MatchReplyWith(CONVERSATIONID);
			ACLMessage msg = receive(template);
			
			if ((msg!=null) && (receiveBoard!=null))	{				
				receiveBoard.setMessage(msg.getContent());
				releaseCommand(receiveBoard);				
			} else {
				block();
			}
			
		}
		
	}
}
