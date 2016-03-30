package at.tuwien.ict.acona.cell.datastructures;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.cell.datastructures.types.Keys;

public class Message {
	public final static String KEYRECEIVER = "RECEIVER";
	public final static String KEYMODE = "MODE";
	public final static String KEYSERVICE = "SERVICE";
	public final static String KEYCONTENT = "CONTENT";
	
	
	private final static Gson gson = new Gson();	
	private final JsonObject jsondatapoint;
	
	private Message() {
		jsondatapoint = new JsonObject();
		this.jsondatapoint.add(KEYRECEIVER, new JsonArray());
		this.jsondatapoint.addProperty(Keys.SERVICE.toString(), AconaService.NONE.toString());
		this.jsondatapoint.addProperty(KEYCONTENT, "");
		this.jsondatapoint.addProperty(Keys.MODE.toString(), AconaSync.ASYNCHRONIZED.toString());
	}
	
	public static Message newMessage() {
		return new Message();
	}
	
//	public static Message createMessage(List<String> receivers, String type, String contentAsJsonString) {
//		return Message.newMessage().addReceivers(receivers).setService(type).setContent(contentAsJsonString);
//	}
	
	public static Message toMessage(JsonObject data) {
		Message result = null;
		
		try {
			if (Message.isMessage(data)==true) {
				result = Message.newMessage()
						.setReceivers(data.get(Message.KEYRECEIVER).getAsJsonArray())
						.setService(AconaService.valueOf(data.get(KEYSERVICE).getAsString()))
						.setMode(AconaSync.valueOf(data.get(KEYMODE).getAsString()))
						.setContent(data.get(Message.KEYCONTENT));
			} else {
				throw new IllegalArgumentException("Cannot cast json data to datapoint");
			}
		} catch (IllegalArgumentException e) {
			throw e;
		}
		
		return result;
	}
	
	public static Message toMessage(String data) {
		Gson gson = new Gson();
		JsonObject jsonData = gson.fromJson(data, JsonObject.class);
		return Message.toMessage(jsonData);
	}
	
	public static boolean isMessage(JsonObject data) {
		boolean result = false;
		
		if (data.has(Message.KEYRECEIVER) && 
				data.has(Message.KEYSERVICE) && 
				data.has(Message.KEYCONTENT) &&
				data.has(Message.KEYMODE)) {
			result = true;
		}
		
		return result;
	}
	
//	public static JsonPrimitive toJsonPrimitive(String key, String value) {
//		JsonObject obj = new JsonObject();
//		obj.addProperty(key, value);
//		return obj.getAsJsonPrimitive(key);
//	}
	
	public static JsonObject toJsonObject(String jsonString) {
		//Create String
		//String jsonString = "{\"" + key + "\":" + jsonString
		
		//Convert to json
		JsonObject jsonData = gson.fromJson(jsonString, JsonObject.class);
		return jsonData;
	}
	
	public static String toJsonStringFromString(String string) {
		JsonObject obj = new JsonObject();
		obj.addProperty(KEYCONTENT, string);
		return obj.toString();
	}
	
	public Message setReceiver(String receiver) {
		return this.setReceivers(Arrays.asList(receiver));
	}
	
	public Message setReceivers(List<String> receivers) {
		if (this.jsondatapoint.get(KEYRECEIVER).getAsJsonArray().size()>0) {
			this.jsondatapoint.remove(KEYRECEIVER);
			this.jsondatapoint.add(KEYRECEIVER, new JsonArray());
		}
		
		return this.addReceivers(receivers);
	}
	
	public Message addReceiver(String receiver) {
		this.jsondatapoint.get(KEYRECEIVER).getAsJsonArray().add(new JsonPrimitive(receiver));
		
		return this;
	}
	
	public Message addReceivers(List<String> receivers) {
		JsonArray receiverArray = new JsonArray();
		for (int i=0; i<receivers.size(); i++) {
			receiverArray.add(new JsonPrimitive(receivers.get(i)));
		}
		
		this.jsondatapoint.get(KEYRECEIVER).getAsJsonArray().addAll(receiverArray);
		
		return this;
	}
	
	public Message setReceivers(JsonArray receivers) {
		this.jsondatapoint.get(KEYRECEIVER).getAsJsonArray().addAll(receivers);
		
		return this;
	}
	
	public Message setService(AconaService type) {
		this.jsondatapoint.addProperty(KEYSERVICE, type.toString());
		
		return this;
	}
	
	public Message setMode(AconaSync mode) {
		this.jsondatapoint.addProperty(KEYMODE, mode.toString());
		
		return this;
	}
	
	public Message setContent(String value) {
		//Check if it is a json object
		boolean isJsonObject = JSONUtils.isJSONValid(value);
		if (isJsonObject==true) {
			JsonObject object = gson.fromJson(value, JsonObject.class);
			this.jsondatapoint.add(KEYCONTENT, object);
		} else {
			this.jsondatapoint.addProperty(KEYCONTENT, value);
		}
		
		return this;
	}
	
	public Message setContent(JsonElement value) {
		this.jsondatapoint.add(KEYCONTENT, value);
		
		return this;
	}
	
	public Message setContent(Datapoint value) {
		this.jsondatapoint.add(KEYCONTENT, value.toJsonObject());
		
		return this;
	}
	
	public JsonObject toJsonObject() {
		return this.jsondatapoint;
	}
	
	public String[] getReceivers() {
		String[] receivers = gson.fromJson(this.jsondatapoint.get(KEYRECEIVER).getAsJsonArray(), String[].class);
		return receivers;
	}
	
	public AconaService getService() {
		return AconaService.valueOf(this.jsondatapoint.get(KEYSERVICE).getAsString());
	}
	
	public AconaSync getMode() {
		return AconaSync.valueOf(this.jsondatapoint.get(KEYMODE).getAsString());
	}
	
	public JsonElement getContent() {
		JsonElement content = this.jsondatapoint.get(KEYCONTENT);
		return content;
	}
	
	public String getStringContent() {
		String content = this.jsondatapoint.get(KEYCONTENT).getAsJsonPrimitive().getAsString();
		return content;
	}

	@Override
	public String toString() {
		return this.jsondatapoint.toString();
	}
	
	
	
}
