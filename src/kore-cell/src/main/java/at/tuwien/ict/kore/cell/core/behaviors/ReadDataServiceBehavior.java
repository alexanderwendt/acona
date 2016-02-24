package at.tuwien.ict.kore.cell.core.behaviors;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.cell.core.CellImpl;
import at.tuwien.ict.kore.cell.datastructures.Datapackage;
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
	
	public ReadDataServiceBehavior() {
		if (this.myAgent instanceof CellImpl) {
			this.callerCell = (CellImpl)this.myAgent;
		} else {
			throw new UnsupportedOperationException ("The creating agent must be an instance of CellImpl");
		}
		
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST).MatchOntology("Read");
		ACLMessage msg = this.callerCell.receive(mt);
		if (msg != null) {
			//Get content, i.e. the address to be read
			String address = msg.getContent();
			log.debug("Received from sender={}, content={}", msg.getSender().toString(), msg.getContent());
			//Read data from storage
			Datapackage readData = this.callerCell.getDataStorage().read(address);
			//Send back
			ACLMessage reply = msg.createReply();
			try {
				reply.setContentObject(readData);
			} catch (IOException e) {
				log.error("Cannot serialize datapackage={}", readData, e);
				reply.setContent("");
				reply.setReplyWith("ERROR");
			}
			
			this.callerCell.send(reply);
			
		} else {
			block();
		} 
	}

}
