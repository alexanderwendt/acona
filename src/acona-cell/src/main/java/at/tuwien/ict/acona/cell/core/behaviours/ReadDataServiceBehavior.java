package at.tuwien.ict.acona.cell.core.behaviours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
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
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchOntology(AconaService.READ.toString()));
		ACLMessage msg = this.callerCell.receive(mt);
		if (msg != null) {
			Datapoint datapoint=null;
			try {
				Message message = ACLUtils.convertToMessage(msg);
				//Get content, i.e. the address to be read
				//String addressMessage = msg.getContent();
				log.debug("Received read request. Message={}", message);
				
				//Convert to datapoint
				datapoint = Datapoint.toDatapoint(message.getContent().getAsJsonObject());
				

				//Get datapointaddress from message
				//String address = datapoint.getAddress(); //JsonMessage.toJson(addressMessage).get(JsonMessage.DATAPOINTADDRESS).getAsString();
				
				//Read data from storage
				Datapoint readData = this.callerCell.getDataStorage().read(datapoint.getAddress());
				//Get value
				//String value = readData.getValue().toString();
				//datapoint.setValue(readData.getValue());
				
				//Send back
				ACLMessage reply = ACLUtils.createReply(msg, readData);
				//reply.setContent(readData.toString());
				
				this.callerCell.send(reply);
				
			} catch (Exception e) {
				log.error("Datapoint error of datapoint={}", datapoint, e);
			}
		} else {
			block();
		} 
	}

}
