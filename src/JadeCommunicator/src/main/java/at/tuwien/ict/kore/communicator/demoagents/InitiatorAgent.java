package at.tuwien.ict.kore.communicator.demoagents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class InitiatorAgent extends Agent {

	private static Logger log = LoggerFactory.getLogger("main");
	
	protected void setup() {
		MDC.put("id", this.getLocalName());
		
		log.debug("Start initiatoragent={}", this.getLocalName());
		String agentName = "";
		String message = "";
		Object[] args = this.getArguments();
		if (args!=null) {
			agentName = (String) args[0];
			message = (String)args[1];
			
			log.debug("received target name={}, send message={}", agentName, message);
		} else {
			throw new NullPointerException("No arguments found");
		}
		
		final String newMessage = message;
		final AID gatewayAgent = new AID(agentName, AID.ISLOCALNAME);
		
		// pong behaviour
		addBehaviour(new TickerBehaviour(this, 1000) {
			
			@Override
			protected void onTick() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(gatewayAgent);
				msg.setContent(newMessage);
				myAgent.send(msg);
				log.info("Message send={} to agent={}", msg.getContent(), msg.getAllReceiver().toString());
			}

		});
	}
}
