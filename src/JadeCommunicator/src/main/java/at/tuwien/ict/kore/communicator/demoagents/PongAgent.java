package at.tuwien.ict.kore.communicator.demoagents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import at.tuwien.ict.kore.communicator.core.JsonMessage;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;

public class PongAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger("main");
	
	protected void setup() {
		
		MDC.put("id", this.getLocalName());
				
		log.debug("Start agent={}", this.getLocalName());
		String returnMessage = "";
		int mode = 0;
		
		Object[] args = this.getArguments();
		if (args!=null) {
			mode = Integer.valueOf((String)args[0]);	//Mode=0: return message in return message, Mode=1: append returnmessage, mode=2: return incoming message 
			returnMessage = (String)args[1];
			
			log.debug("agent will use message={} and has mode={}", returnMessage, mode);
		} else {
			throw new NullPointerException("No arguments found although necessary. Add mode and message");
		}
		
		final String newMessage = returnMessage;
		final int newMode = mode;
		
		// pong behaviour
		addBehaviour(new CyclicBehaviour(this) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action() {
				ACLMessage msg = receive();
				String content= "";
				
				if (msg!=null) {
					switch (newMode) {
						case 0: 
							content = newMessage;
							break;
						case 1:
							content = JsonMessage.toJson(msg.getContent()).get(JsonMessage.CONTENT).getAsString() + newMessage;
							break;
						case 2:
							content = JsonMessage.toJson(msg.getContent()).get(JsonMessage.CONTENT).getAsString();
							break;
						default:
						try {
							throw new Exception("Errorneous input" + newMode);
						} catch (Exception e1) {
							log.error("Erroneous input", e1);
						}
					}
					
					ACLMessage reply = msg.createReply();
					reply.setPerformative( ACLMessage.INFORM);
					reply.setContent(JsonMessage.toContentString(content));
					
					for (int i=0;i<3;i++) {
						synchronized(this) {
							try {
								this.wait(100);
								log.debug("Wait {}00ms", i+1);
							} catch (InterruptedException e) {
								
							}
						}
					}
					
					send(reply);
					log.info("Response={}", content);
				} else {
					block();
				}
			}
		});
		
	}
		
	 protected void takeDown() {
		 try { 
			  DFService.deregister(this);
			  log.info("{} removed", this.getAID().getLocalName());
		 } 	catch (Exception e) {
			 log.error("Cannot deregister agent", e);
		 }
	 }	
}
