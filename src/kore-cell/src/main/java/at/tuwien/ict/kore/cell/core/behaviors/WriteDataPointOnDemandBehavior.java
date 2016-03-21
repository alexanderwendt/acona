package at.tuwien.ict.kore.cell.core.behaviors;

import com.google.gson.JsonObject;

import at.tuwien.ict.kore.communicator.core.JsonMessage;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class WriteDataPointOnDemandBehavior extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final AID receiver;
	private final String datapointAddress;
	private final String value;
	
	
	public WriteDataPointOnDemandBehavior(AID receiver, String datapointAddress, String value) {
		this.receiver = receiver;
		this.datapointAddress = datapointAddress;
		this.value = value;
	}
	
	@Override
	public void action() {
		//Write value of datapoint to subscriber, in order to provide with initial value
		//Create send message without target
		ACLMessage notifyMessage = new ACLMessage(ACLMessage.REQUEST);
		notifyMessage.setOntology(JsonMessage.SERVICEWRITE);
		//Create message body
		JsonObject writeBody = new JsonObject();
		writeBody.addProperty(JsonMessage.DATAPOINTADDRESS, datapointAddress);
		writeBody.addProperty(JsonMessage.VALUE, value);
		notifyMessage.setContent(writeBody.toString());
		notifyMessage.addReceiver(receiver);
		this.myAgent.send(notifyMessage);
		
	}

}
