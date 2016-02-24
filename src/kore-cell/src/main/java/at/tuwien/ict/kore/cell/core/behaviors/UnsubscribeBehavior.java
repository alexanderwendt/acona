package at.tuwien.ict.kore.cell.core.behaviors;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UnsubscribeBehavior extends CyclicBehaviour {

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			// CFP Message received. Process it
			
		}
		else {
			block();
		} 
		
		
	}

}
