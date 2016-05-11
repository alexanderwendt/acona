package at.tuwien.ict.acona.cell.core.behaviours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapackage;
import at.tuwien.ict.acona.cell.datastructures.DatapackageImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SubscribeDataServiceBehavior extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(SubscribeDataServiceBehavior.class);
	
	private final CellImpl callerCell; 
	
	public SubscribeDataServiceBehavior(CellImpl caller) {
		this.callerCell = caller;
	}
	

	@Override
	public void action() {
		// TODO Auto-generated method stub
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchOntology(AconaService.SUBSCRIBE.toString()));
		ACLMessage msg = this.callerCell.receive(mt);
		if (msg != null) {
			try {
				Message message = ACLUtils.convertToMessage(msg);
				
				//Get content, i.e. the address to be read
				Datapoint dp = message.getContentAsDatapoint(); //Datapoint.toDatapoint(msg.getContent());
				log.debug("Subscribe request: received from sender={}, content={}", msg.getSender().toString(), msg.getContent());
				
				//Get datapointaddress from message
				//Format, address, value, callerID
				//String address = JsonMessage.toJson(inputMessage).get(JsonMessage.DATAPOINTADDRESS).getAsString();
				//String value = JsonMessage.toJson(inputMessage).get(JsonMessage.VALUE).getAsString();
				
				//Datapackage dataPackageToWrite = DatapackageImpl.newDatapackage(address, value);
				//Read data from storage
				this.callerCell.getDataStorage().subscribeDatapoint(dp.getAddress(), msg.getSender().getLocalName());
				
				log.debug("caller={} subscribes={}", msg.getSender().getLocalName(), dp.getAddress());
				
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
				
				this.myAgent.addBehaviour(new WriteDataPointOnDemandBehavior(msg.getSender(), this.callerCell.getDataStorage().read(dp.getAddress())));
				log.debug("Initial value sent to {}", msg.getSender().getLocalName());
				
			} catch (Exception e) {
				log.error("Cannot write data", e);
				//throw e;
			}
		} else {
			block();
		} 
	}

}
