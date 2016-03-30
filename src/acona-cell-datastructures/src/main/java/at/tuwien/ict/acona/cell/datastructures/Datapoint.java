package at.tuwien.ict.acona.cell.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Datapoint {
	private final static String KEYADDRESS = "ADDRESS";
	private final static String KEYTYPE = "TYPE";
	private final static String KEYVALUE = "VALUE";
	
	private final static Gson gson = new Gson();
	private final JsonObject jsondatapoint;
	
	private static Logger log = LoggerFactory.getLogger(Datapoint.class);
	
	private Datapoint(String address) {
		jsondatapoint = new JsonObject();
		this.jsondatapoint.addProperty(KEYADDRESS, address);
		this.jsondatapoint.addProperty(KEYTYPE, "");
		this.jsondatapoint.addProperty(KEYVALUE, "");
	}
	
	public static Datapoint newDatapoint(String address) {
		return new Datapoint(address);
	}
	
	public static Datapoint toDatapoint(JsonObject data) throws IllegalArgumentException {
		Datapoint result = null;
		
		try {
			if (Datapoint.isDatapoint(data)==true) {
				result = Datapoint.newDatapoint(data.get(KEYADDRESS).getAsString()).setType(data.get(KEYTYPE).getAsString()).setValue(data.get(KEYVALUE));
			} else {
				throw new IllegalArgumentException("Cannot cast json data to datapoint");
			}
			
		} catch (IllegalArgumentException e) {
			throw e;
		}
		
		return result;
	}
	
	public static Datapoint toDatapoint(String data) throws Exception {
		log.debug("Datapoint to convert={}", data);
		JsonObject jsonData = gson.fromJson(data, JsonObject.class);
		return Datapoint.toDatapoint(jsonData);
	}
	
	public static boolean isDatapoint(JsonObject data) {
		boolean result = false;
		
		if (data.has(KEYADDRESS) && data.has(KEYTYPE) && data.has(KEYVALUE)) {
			result = true;
		}
		
		return result;
	}
	
	public Datapoint setType(String type) {
		this.jsondatapoint.addProperty(KEYTYPE, type);
		
		return this;
	}
	
	public Datapoint setValue(String value) {
		this.jsondatapoint.addProperty(KEYVALUE, value);
		
		return this;
	}
	
	public Datapoint setValue(JsonElement value) {
		this.jsondatapoint.add(KEYVALUE, value);
		
		return this;
	}
	
	public JsonObject toJsonObject() {
		return this.jsondatapoint;
	}
	
	public String getAddress() {
		return this.jsondatapoint.get(KEYADDRESS).getAsString();
	}
	
	public String getType() {
		return this.jsondatapoint.get(KEYTYPE).getAsString();
	}
	
	public JsonElement getValue() {
		return this.jsondatapoint.get(KEYVALUE);
	}

	@Override
	public String toString() {
		return this.jsondatapoint.toString();
	}
	
	
	
	
}
