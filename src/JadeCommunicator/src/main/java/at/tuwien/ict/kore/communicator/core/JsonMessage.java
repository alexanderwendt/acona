package at.tuwien.ict.kore.communicator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class JsonMessage {
	public final static String SYNCREQUEST = "EXTERNALSYNCREQUEST";
	public final static String RECEIVER = "RECEIVER";
	public final static String TYPE = "TYPE";
	public final static String CONTENT = "CONTENT";
	public final static String BODY = "BODY";
	public final static String DATAPOINTADDRESS = "DATAPOINT";
	public final static String VALUE = "VALUE";
	public final static String SERVICEREAD = "READ";
	public final static String SERVICEWRITE = "WRITE";
	public final static String SERVICESUBSCRIBE = "SUBSCRIBE";
	public final static String SERVICEUNSUBSCRIBE = "UNSUBSCRIBE";
	public final static String ACKNOWLEDGE = "ACKNOWLEDGE";
	
	public static JsonObject createMessage(String bodyAsJsonString, String receiver, String type) {
		return createMessage(bodyAsJsonString, Arrays.asList(receiver), type);
	}
	
	public static JsonObject createMessage(String bodyAsJsonString, List<String> receivers, String type) {
		
		//JsonObject contentValue = new JsonObject();
		//contentValue.addProperty(JsonMessage.CONTENT, content);
		
		JsonObject result = new JsonObject();
		result.add(BODY, toJson(bodyAsJsonString));
		JsonArray receiverArray = new JsonArray();
		for (int i=0; i<receivers.size(); i++) {
			receiverArray.add(new JsonPrimitive(receivers.get(i)));
		}
		
		result.add(RECEIVER, receiverArray);
		result.addProperty(TYPE, type);
		
		return result;
	}
	
	public static JsonObject createMessage(JsonObject bodyAsJsonString, List<String> receivers, String type) {
		
		//JsonObject contentValue = new JsonObject();
		//contentValue.addProperty(JsonMessage.CONTENT, content);
		
		JsonObject result = new JsonObject();
		result.add(BODY, bodyAsJsonString);
		JsonArray receiverArray = new JsonArray();
		for (int i=0; i<receivers.size(); i++) {
			receiverArray.add(new JsonPrimitive(receivers.get(i)));
		}
		
		result.add(RECEIVER, receiverArray);
		result.addProperty(TYPE, type);
		
		return result;
	}
	
	public static String[] getReceivers(JsonObject message) {
		Gson gson = new Gson();
		String[] receivers = gson.fromJson(message.get(RECEIVER).getAsJsonArray(), String[].class);
		
		return receivers;
	}
	
	public static JsonObject getBody(JsonObject body) {
		return body.getAsJsonObject(BODY);
	}
	
	public static String getType(JsonObject message) {
		return message.get(TYPE).getAsString();
	}
	
	public static JsonObject convertToJson(ACLMessage message) {
		
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
		
		JsonObject result = createMessage(content, localReceivers, type);
		
		return result;
	}
	
	public static ACLMessage convertToACL(JsonObject message) {
		//Set the receiver and send the command to the receiver
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	//TODO exchange to performative in the Json message!!!
		
		String[] receivers = getReceivers(message);
		
		for (int i=0; i<receivers.length;i++) {
			msg.addReceiver(new AID(receivers[i], AID.ISLOCALNAME));
		}
		
		//msg.setConversationId(CONVERSATIONID);
		//msg.setConversationId(JsonMessage.SYNCREQUEST);
		String content = message.get(BODY).toString();
		msg.setContent(content);
		msg.setOntology(message.get(JsonMessage.TYPE).getAsString());	//Ontology used as type
		
		return msg;
	}
	
	public static String toContentString(String input) {
		return toJsonObjectString(CONTENT, input);
	}
	
	public static String toJsonObjectString(String name, String value) {
		JsonObject contentValue = new JsonObject();
		contentValue.addProperty(name, value);
		
		return contentValue.toString();
	}
	
	public static JsonObject toJson(String contentObject) {
		//Parse to JSON
		Gson gson = new Gson();
		JsonObject result = gson.fromJson(contentObject, JsonObject.class);
		return result;
	}
}
