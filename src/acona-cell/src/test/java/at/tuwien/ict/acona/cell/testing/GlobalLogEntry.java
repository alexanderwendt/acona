package at.tuwien.ict.acona.cell.testing;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class GlobalLogEntry{
	public GlobalLogEntry(AID agent, ACLMessage message) {
		this.agent = agent;
		this.message = message;
	}
	public final AID agent;
	public final ACLMessage message;
	
	public AID getAgent() {
		return agent;
	}

	public ACLMessage getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return agent.getLocalName() + ": " + message.getContent().toString() + "\n";
	}
}
