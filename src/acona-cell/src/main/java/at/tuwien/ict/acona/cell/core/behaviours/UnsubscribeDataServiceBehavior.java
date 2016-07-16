package at.tuwien.ict.acona.cell.core.behaviours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UnsubscribeDataServiceBehavior extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(SubscribeDataServiceBehavior.class);
	
	private final CellImpl callerCell; 
	
	public UnsubscribeDataServiceBehavior(CellImpl caller) {
		this.callerCell = caller;
	}
	

	@Override
	public void action() {
		// TODO Auto-generated method stub
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchOntology(AconaServiceType.UNSUBSCRIBE.toString()));
		ACLMessage msg = this.callerCell.receive(mt);
		if (msg != null) {
			try {
				Message message = ACLUtils.convertToMessage(msg);
				
				//Get content, i.e. the address to be read
				Datapoint dp = Datapoint.toDatapoint(message.getContent().getAsJsonObject());
				log.debug("Unsubscribe request: received from sender={}, content={}", msg.getSender().toString(), msg.getContent());
				
				//Read data from storage
				this.callerCell.getDataStorage().unsubscribeDatapoint(dp.getAddress(), msg.getSender().getLocalName());
				
				log.debug("caller={} unsubscribes={}", msg.getSender().getLocalName(), dp.getAddress());
				
				//Send back if synchronized call
				if (message.getMode().equals(AconaSync.SYNCHRONIZED)) {
					ACLMessage reply = msg.createReply();
					reply.setReplyWith(msg.getReplyWith());
					reply.setPerformative(ACLMessage.CONFIRM);
					String replyMessage = "ACK";
					reply.setContent(replyMessage);
					ACLUtils.enhanceACLMessageWithCustomParameters(reply, message);
					
					this.callerCell.send(reply);
					log.debug("Reply sent");
				}
			} catch (Exception e) {
				log.error("Cannot unsubscribe", e);
				//throw e;
			}
		} else {
			block();
		} 
	}


}
