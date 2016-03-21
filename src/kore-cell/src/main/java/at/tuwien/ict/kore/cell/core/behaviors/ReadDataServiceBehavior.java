package at.tuwien.ict.kore.cell.core.behaviors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.kore.cell.core.CellImpl;
import at.tuwien.ict.kore.cell.datastructures.Datapackage;
import at.tuwien.ict.kore.communicator.core.JsonMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReadDataServiceBehavior extends CyclicBehaviour {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(ReadDataServiceBehavior.class);
	
	private final CellImpl callerCell; 
	
	public ReadDataServiceBehavior(CellImpl caller) {
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
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchOntology(JsonMessage.SERVICEREAD));
		ACLMessage msg = this.callerCell.receive(mt);
		if (msg != null) {
			//Get content, i.e. the address to be read
			String addressMessage = msg.getContent();
			log.debug("Received from sender={}, content={}", msg.getSender().toString(), msg.getContent());
			
			//Get datapointaddress from message
			String address = JsonMessage.toJson(addressMessage).get(JsonMessage.DATAPOINTADDRESS).getAsString();
			
			//Read data from storage
			Datapackage readData = this.callerCell.getDataStorage().read(address);
			//Get value
			String value = readData.get(address).getDefaultValue();
			
			//Send back
			ACLMessage reply = msg.createReply();
			reply.setContent(JsonMessage.toJsonObjectString(address, value));
			
			this.callerCell.send(reply);
			
		} else {
			block();
		} 
	}

}
