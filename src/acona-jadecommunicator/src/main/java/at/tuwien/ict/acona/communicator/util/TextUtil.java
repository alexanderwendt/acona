package at.tuwien.ict.acona.communicator.util;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class TextUtil {
	public String toShortMessage(ACLMessage longMessage) {
		//String result = "";
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("snd: ").append(longMessage.getSender().getLocalName()).append("; rcv:");
		//Iterator<?> iter = longMessage.getAllReceiver();
		//+ ", rcv: " 

		longMessage.getAllReceiver().forEachRemaining(n -> {builder.append(" " + ((AID)n).getLocalName());});
		builder.append("Perf: ").append(ACLMessage.getPerformative(longMessage.getPerformative()));
		builder.append("; content: ").append(longMessage.getContent());
		
		return builder.toString();
		
	}
}
