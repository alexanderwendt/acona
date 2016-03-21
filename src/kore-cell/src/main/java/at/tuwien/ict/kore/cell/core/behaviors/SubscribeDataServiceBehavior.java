package at.tuwien.ict.kore.cell.core.behaviors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.kore.cell.core.CellImpl;
import at.tuwien.ict.kore.cell.datastructures.Datapackage;
import at.tuwien.ict.kore.cell.datastructures.DatapackageImpl;
import at.tuwien.ict.kore.communicator.core.JsonMessage;
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
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchOntology(JsonMessage.SERVICESUBSCRIBE));
		ACLMessage msg = this.callerCell.receive(mt);
		if (msg != null) {
			try {
				//Get content, i.e. the address to be read
				String inputMessage = msg.getContent();
				log.debug("Subscribe request: received from sender={}, content={}", msg.getSender().toString(), msg.getContent());
				
				//Get datapointaddress from message
				//Format, address, value, callerID
				String address = JsonMessage.toJson(inputMessage).get(JsonMessage.DATAPOINTADDRESS).getAsString();
				//String value = JsonMessage.toJson(inputMessage).get(JsonMessage.VALUE).getAsString();
				
				//Datapackage dataPackageToWrite = DatapackageImpl.newDatapackage(address, value);
				//Read data from storage
				this.callerCell.getDataStorage().subscribeDatapoint(address, msg.getSender().getLocalName());
				
				log.debug("caller={} subscribes={}", msg.getSender().getLocalName(), address);
				
				//Send back
//				ACLMessage reply = msg.createReply();
//				reply.setReplyWith(msg.getReplyWith());
//				String replyMessage = JsonMessage.toContentString(JsonMessage.ACKNOWLEDGE);
//				reply.setPerformative(ACLMessage.CONFIRM);
//				reply.setContent(replyMessage);
//				
//				this.callerCell.send(reply);
//				log.debug("Reply sent");
				
				
				
//				//Write value of datapoint to subscriber, in order to provide with initial value
//				//Create send message without target
				
				//Create message body
//				JsonObject writeBody = new JsonObject();
//				writeBody.addProperty(JsonMessage.DATAPOINTADDRESS, address);
//				writeBody.addProperty(JsonMessage.VALUE, this.callerCell.getDataStorage().read(address).get(address).getDefaultValue());
				
				this.myAgent.addBehaviour(new WriteDataPointOnDemandBehavior(msg.getSender(), address, this.callerCell.getDataStorage().read(address).get(address).getDefaultValue()));
				log.debug("Initial value sent to {}", msg.getSender().getLocalName());
//				ACLMessage notifyMessage = new ACLMessage(ACLMessage.REQUEST);
//				notifyMessage.setOntology(JsonMessage.SERVICEWRITE);
//				
//				notifyMessage.setContent(writeBody.toString());
//				notifyMessage.addReceiver(msg.getSender());
//				this.callerCell.send(notifyMessage);
				
			} catch (Exception e) {
				log.error("Cannot write data", e);
				throw e;
			}
		} else {
			block();
		} 
	}

}
