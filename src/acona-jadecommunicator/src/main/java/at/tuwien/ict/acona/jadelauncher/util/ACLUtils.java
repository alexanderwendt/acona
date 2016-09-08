package at.tuwien.ict.acona.jadelauncher.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.cell.datastructures.types.Keys;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ACLUtils {
	
	public static Message convertToMessage(ACLMessage message) {
		
		String content = message.getContent(); //as String
		//AconaService type = AconaService.valueOf(message.getUserDefinedParameter(Keys.SERVICE.toString())); //for type
		AconaServiceType type = AconaServiceType.valueOf(message.getOntology()); //for type
		//AconaSync mode = AconaSync.valueOf(message.getUserDefinedParameter(Keys.MODE.toString())); //for type
		AconaSync mode = AconaSync.valueOf(message.getEncoding()); //for type
		//Get all receivers in a list
		List<Object> receivers = new ArrayList<Object>();
		List<String> localReceivers = new ArrayList<String>();
		message.getAllReceiver().forEachRemaining(receivers::add);	//java 8
		receivers.forEach((Object o)->{
			String name=((AID)o).getLocalName();
			localReceivers.add(name);
		});
		
		//Check if content String is a JSON file
		
		
		Message result = Message.newMessage().setReceivers(localReceivers).setService(type).setContent(content).setMode(mode); 
		
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
		//Check if JSON or normal string
		String content = "";
		if (message.getContent().isJsonPrimitive()==true) {
			content = message.getContent().getAsJsonPrimitive().getAsString();
		} else {
			content = message.getContent().toString();
		}
		
		msg.setContent(content);
		//msg.addUserDefinedParameter(Keys.SERVICE.toString(), message.getService().toString());	//Ontology used as type
		msg.setOntology(message.getService().toString());
		//msg.addUserDefinedParameter(Keys.MODE.toString(), message.getMode().toString());
		msg.setEncoding(message.getMode().toString());
		
		return msg;
	}
	
	public static void enhanceACLMessageWithCustomParameters(ACLMessage reply, Message message) {
		//reply.addUserDefinedParameter(Keys.SERVICE.toString(), message.getService().toString());	//Ontology used as type
		//reply.addUserDefinedParameter(Keys.MODE.toString(), message.getMode().toString());
		reply.setOntology(message.getService().toString());
		reply.setEncoding(message.getMode().toString());
	}
	
//	public static ACLMessage createReply(ACLMessage originalMessage, Datapoint content) {
//		ACLMessage replyTemplate = originalMessage.createReply();
//		replyTemplate.setEncoding(originalMessage.getEncoding());
//		replyTemplate.setOntology(originalMessage.getOntology());
//		
//		Message replyMessage = ACLUtils.convertToMessage(replyTemplate);
//		replyMessage.setContent(content);
//		//replyMessage.setMode(input.getMode());
//		//replyMessage.setService(input.getService());
//		
//		//ACLUtils.enhanceACLMessageWithCustomParameters(reply, input);
//		//reply.setContent(content.getValue().toString());
//		
//		ACLMessage reply = ACLUtils.convertToACL(replyMessage);
//		
//		return reply;
//	}
	
//	public static ACLMessage prepareResponseServiceBehaviours(AconaServiceType serviceType, Logger log, ACLMessage request, List<Datapoint> datapointList) {
//		log.debug("Received message={}", request);
//		ACLMessage temp = request.createReply();
//		//temp.setOntology(AconaServiceType.NONE.toString());
//		temp.setOntology(AconaServiceType.NONE.toString());
//		
//		try { 
//			//Extract datapoints
//			String content = request.getContent();
//			//Type listOfTestObject = new TypeToken<List<Datapoint>>(){}.getType();
//			//String serializedDatapoints = gson.toJson(datapoints, listOfTestObject);
//			JsonArray object = gson.fromJson(content, JsonArray.class);
//			this.datapointList = new ArrayList<Datapoint>();
//			object.forEach(e->{this.datapointList.add(Datapoint.toDatapoint((JsonObject)e));});
//			//datapointList = gson.fromJson(content, listOfTestObject);
//			//List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
//			sender = request.getSender().getLocalName();
//			
//			
//			temp.setPerformative(ACLMessage.AGREE);
//			log.info("OK to write");
//		
//		} catch (Exception fe){
//			log.error("Error handling the WRITE action.", fe);
//			temp.setPerformative(ACLMessage.REFUSE);
//		}
//	}
}
