package at.tuwien.ict.acona.cell.core.behaviours;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendDatapointOnDemandBehavior extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final AID receiver;
	private final Datapoint datapoint;
	private final AconaServiceType service;

	
	
	public SendDatapointOnDemandBehavior(AID receiver, Datapoint datapoint, AconaServiceType serviceToUse) {
		this.receiver = receiver;
		this.datapoint = datapoint;
		this.service = serviceToUse;
	}
	
	@Override
	public void action() {
		//Write value of datapoint to subscriber, in order to provide with initial value
		//Create send message without target
		ACLMessage notifyMessage = ACLUtils.convertToACL(Message.newMessage()
				.setService(this.service)
				.setContent(datapoint.toString()));
		notifyMessage.setPerformative(ACLMessage.REQUEST);
		//ACLMessage notifyMessage = new ACLMessage(ACLMessage.REQUEST);
		//notifyMessage.setOntology(JsonMessage.SERVICEWRITE);
		//Create message body
		//JsonObject writeBody = new JsonObject();
		//writeBody.addProperty(JsonMessage.DATAPOINTADDRESS, datapointAddress);
		//writeBody.addProperty(JsonMessage.VALUE, value);
		//notifyMessage.setContent(writeBody.toString());
		notifyMessage.addReceiver(receiver);
		this.myAgent.send(notifyMessage);
		
	}
}
