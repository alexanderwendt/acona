package at.tuwien.ict.acona.cell.core.behaviors;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class WriteDataPointOnDemandBehavior extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final AID receiver;
	private final Datapoint datapoint;

	
	
	public WriteDataPointOnDemandBehavior(AID receiver, Datapoint datapoint) {
		this.receiver = receiver;
		this.datapoint = datapoint;
	}
	
	@Override
	public void action() {
		//Write value of datapoint to subscriber, in order to provide with initial value
		//Create send message without target
		ACLMessage notifyMessage = ACLUtils.convertToACL(Message.newMessage()
				.setService(AconaService.WRITE)
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
