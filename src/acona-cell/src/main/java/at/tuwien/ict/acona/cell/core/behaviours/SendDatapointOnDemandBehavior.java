package at.tuwien.ict.acona.cell.core.behaviours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendDatapointOnDemandBehavior extends OneShotBehaviour {
	
	private static Logger log = LoggerFactory.getLogger(SendDatapointOnDemandBehavior.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final AID receiver;
	private final Datapoint datapoint;
	private final AconaService service;

	
	
	/**
	 * Send a message to another agent with a oneshotbehaviour
	 * 
	 * @param receiver AID from receiver
	 * @param datapoint set the datapoint
	 * @param serviceToUse set which acona service to use
	 */
	public SendDatapointOnDemandBehavior(AID receiver, Datapoint datapoint, AconaService serviceToUse) {
		this.receiver = receiver;
		this.datapoint = datapoint;
		this.service = serviceToUse;
	}
	
	@Override
	public void action() {
		log.trace("Start sendDatapointondemandbehaviour");
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
		log.debug("Datapoint={} sent to agent={} with service={}", datapoint, receiver, service);
	}
}
