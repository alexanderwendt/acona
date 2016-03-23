package at.tuwien.ict.acona.communicator.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Message;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ACLUtils {
	
	public static Message convertToMessage(ACLMessage message) {
		
		String content = message.getContent(); //as String
		String type = message.getOntology(); //for type
		//Get all receivers in a list
		List<Object> receivers = new ArrayList<Object>();
		List<String> localReceivers = new ArrayList<String>();
		message.getAllReceiver().forEachRemaining(receivers::add);	//java 8
		receivers.forEach((Object o)->{
			String name=((AID)o).getLocalName();
			localReceivers.add(name);
		});
		
		Message result = Message.newMessage().setReceivers(localReceivers).setService(type).setContent(content); 
		
		return result;
	}
	
	public static ACLMessage convertToACL(Message message) {
		//Set the receiver and send the command to the receiver
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	//TODO exchange to performative in the Json message!!!
		
		String[] receivers = message.getReceivers();
		
		for (int i=0; i<receivers.length;i++) {
			msg.addReceiver(new AID(receivers[i], AID.ISLOCALNAME));
		}
		
		//msg.setConversationId(CONVERSATIONID);
		//msg.setConversationId(JsonMessage.SYNCREQUEST);
		String content = message.getContent().toString();
		msg.setContent(content);
		msg.setOntology(message.getService());	//Ontology used as type
		
		return msg;
	}
}
