package at.tuwien.ict.acona.cell.core.behaviors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WriteDataServiceBehavior extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(WriteDataServiceBehavior.class);
	
	private final CellImpl callerCell; 
	
	public WriteDataServiceBehavior(CellImpl caller) {
		//super();
		//if (this.myAgent instanceof CellImpl) {
		this.callerCell = caller;
		//} else {
		//	throw new UnsupportedOperationException ("The creating agent must be an instance of CellImpl");
		//}
		
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchOntology(AconaService.WRITE.toString()));
		ACLMessage msg = this.callerCell.receive(mt);
		if (msg != null) {
			try {
				Message message = ACLUtils.convertToMessage(msg);
				
				//Get content, i.e. the address to be read
				//String inputMessage = msg.getContent();
				log.debug("Received message={}", message);
				
				//Create datapoint
				//log.debug("Inputmessage to write={}", inputMessage);
				Datapoint dp = Datapoint.toDatapoint(message.getContent().getAsJsonObject());
				log.debug("Message converted to datapoint={}", dp);
				//Get datapointaddress from message
				//Format, address, value, callerID
				//String address = JsonMessage.toJson(inputMessage).get(JsonMessage.DATAPOINTADDRESS).getAsString();
				//String value = JsonMessage.toJson(inputMessage).get(JsonMessage.VALUE).getAsString();
				
				//Datapackage dataPackageToWrite = DatapackageImpl.newDatapackage(address, value);
				//Read data from storage
				this.callerCell.getDataStorage().write(dp, msg.getSender().getLocalName());
				
				log.debug("data written={}", dp);
				
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
				log.error("Cannot write data", e);
				//throw e;
			}
		} else {
			block();
		} 
	}

}
