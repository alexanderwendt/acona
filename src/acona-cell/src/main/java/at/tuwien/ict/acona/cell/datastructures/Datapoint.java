package at.tuwien.ict.acona.cell.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Datapoint {
	private final static String KEYADDRESS = "ADDRESS";
	private final static String KEYAGENT = "AGENT";
	private final static String KEYVALUE = "VALUE";

	private final static String NULLADDRESS = "NULLDATAPOINT";

	//TODO: Create datapoints that can take Chunks and Chunk arrays and Json arrays

	private String ADDRESS = "";
	//private String TYPE = "";
	private JsonElement VALUE = new JsonObject(); // new JsonObject();

	private final static Gson gson = new Gson();
	// private final JsonObject jsondatapoint;

	private final static Logger log = LoggerFactory.getLogger(Datapoint.class);

	/**
	 * Create a datapoint from an address
	 * 
	 * @param address:
	 *            The following syntax can be used: x.x for local datapoints,
	 *            [agent]:[localaddress] for global datapoints
	 */
	private Datapoint(String address) {
		// VALUE = new JsonObject();
		this.ADDRESS = address;
		// this.jsondatapoint.addProperty(KEYADDRESS, address);
		// this.jsondatapoint.addProperty(KEYTYPE, "");
		// this.jsondatapoint.addProperty(KEYVALUE, "");
	}

	public static Datapoint newNullDatapoint() {
		return new Datapoint(NULLADDRESS);
	}

	public static Datapoint newDatapoint(String address) {
		return new Datapoint(address);
	}

	public static Datapoint toDatapoint(JsonObject data) throws IllegalArgumentException {
		Datapoint result = null;

		try {
			if (Datapoint.isDatapoint(data) == true) {
				result = Datapoint.newDatapoint(data.get(KEYADDRESS).getAsString()).setValue(data.get(KEYVALUE));
				//.setType(data.get(KEYTYPE).getAsString()).setValue(data.get(KEYVALUE));
			} else {
				throw new IllegalArgumentException("Cannot cast json data to datapoint " + data);
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

		if (data.has(KEYADDRESS) && data.has(KEYVALUE)) {
			result = true;
		}

		return result;
	}

	public static boolean isNullDatapoint(JsonObject data) {
		boolean result = false;

		if (data.has(KEYADDRESS) && data.get(KEYADDRESS).equals(NULLADDRESS)) {
			result = true;
		}

		return result;
	}

	public boolean isNullDatapoint() {
		return isNullDatapoint(this.toJsonObject());
	}

	//	public Datapoint setType(String type) {
	//		this.TYPE = type;
	//
	//		return this;
	//	}

	public Datapoint setValue(String value) {
		this.VALUE = new JsonPrimitive(value);

		return this;
	}

	public Datapoint setValue(JsonElement value) {
		this.VALUE = value;

		return this;
	}

	public <T> Datapoint setValue(T value) {
		this.VALUE = gson.toJsonTree(value);

		return this;
	}

	public JsonObject toJsonObject() {
		return gson.fromJson(this.toJsonString(), JsonObject.class);
	}

	public String toJsonString() {
		return gson.toJson(this, Datapoint.class);
	}

	public String getAddress() {
		return ADDRESS;
	}

	//	public String getType() {
	//		return TYPE;
	//	}

	public JsonElement getValue() {
		return VALUE;
	}

	public <T> T getValue(Class<T> clzz) {
		return gson.fromJson(this.VALUE, clzz);
	}

	public String getValueAsString() {
		String result = "";
		if (VALUE.toString().equals("{}") == true) {
			result = "";
		} else {
			result = VALUE.getAsJsonPrimitive().getAsString();
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.ADDRESS);
		builder.append(":");
		//		if (this.getType().equals("") == false) {
		//			builder.append(this.TYPE);
		//			builder.append(":");
		//		}

		// Check size of object
		if (this.VALUE.toString().length() > 1000) {
			builder.append(this.VALUE.toString().substring(0, 1000));
			builder.append("...");
		} else {
			builder.append(this.VALUE);
		}

		// gson.toJson(this, Datapoint.class);

		return builder.toString();
	}

}
