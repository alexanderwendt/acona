package at.tuwien.ict.acona.cell.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class _OLD_Datapoint {
	private final static String KEYADDRESS = "ADDRESS";
	private final static String KEYTYPE = "TYPE";
	private final static String KEYVALUE = "VALUE";
	
	private final static Gson gson = new Gson();
	private final JsonObject jsondatapoint;
	
	private static Logger log = LoggerFactory.getLogger(_OLD_Datapoint.class);
	
	private _OLD_Datapoint(String address) {
		jsondatapoint = new JsonObject();
		this.jsondatapoint.addProperty(KEYADDRESS, address);
		this.jsondatapoint.addProperty(KEYTYPE, "");
		this.jsondatapoint.addProperty(KEYVALUE, "");
	}
	
	public static _OLD_Datapoint newDatapoint(String address) {
		return new _OLD_Datapoint(address);
	}
	
	public static _OLD_Datapoint toDatapoint(JsonObject data) throws IllegalArgumentException {
		_OLD_Datapoint result = null;
		
		try {
			if (_OLD_Datapoint.isDatapoint(data)==true) {
				result = _OLD_Datapoint.newDatapoint(data.get(KEYADDRESS).getAsString()).setType(data.get(KEYTYPE).getAsString()).setValue(data.get(KEYVALUE));
			} else {
				throw new IllegalArgumentException("Cannot cast json data to datapoint");
			}
			
		} catch (IllegalArgumentException e) {
			throw e;
		}
		
		return result;
	}
	
	public static _OLD_Datapoint toDatapoint(String data) throws Exception {
		log.debug("Datapoint to convert={}", data);
		JsonObject jsonData = gson.fromJson(data, JsonObject.class);
		return _OLD_Datapoint.toDatapoint(jsonData);
	}
	
	public static boolean isDatapoint(JsonObject data) {
		boolean result = false;
		
		if (data.has(KEYADDRESS) && data.has(KEYTYPE) && data.has(KEYVALUE)) {
			result = true;
		}
		
		return result;
	}
	
	public _OLD_Datapoint setType(String type) {
		this.jsondatapoint.addProperty(KEYTYPE, type);
		
		return this;
	}
	
	public _OLD_Datapoint setValue(String value) {
		this.jsondatapoint.addProperty(KEYVALUE, value);
		
		return this;
	}
	
	public _OLD_Datapoint setValue(JsonElement value) {
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
