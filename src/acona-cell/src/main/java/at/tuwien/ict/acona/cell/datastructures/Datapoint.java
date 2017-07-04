package at.tuwien.ict.acona.cell.datastructures;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Datapoint {
	public final static String KEYADDRESS = "ADDRESS";
	public final static String KEYAGENT = "AGENT";
	public final static String KEYVALUE = "VALUE";

	//TODO: Create datapoints that can take Chunks and Chunk arrays and Json arrays

	private String ADDRESS = "";
	//private String TYPE = "";
	private JsonElement VALUE = new JsonObject(); // new JsonObject();

	private transient Gson gson = new Gson(); //Add transient not to serialize this
	// private final JsonObject jsondatapoint;

	//private final static Logger log = LoggerFactory.getLogger(Datapoint.class);

	/**
	 * Create a datapoint from an address
	 * 
	 * @param address:
	 *            The following syntax can be used: x.x for local datapoints,
	 *            [agent]:[localaddress] for global datapoints
	 */
	protected Datapoint(String address) {
		this.ADDRESS = address;
	}

	public Datapoint setValue(String value) {
		this.VALUE = new JsonPrimitive(value);

		return this;
	}

	public Datapoint setValue(JsonElement value) {
		this.VALUE = value;

		return this;
	}

	public <T> Datapoint setValue(T value) {
		if (gson == null) {
			gson = new Gson();
		}

		this.VALUE = gson.toJsonTree(value);

		return this;
	}

	public JsonObject toJsonObject() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson.fromJson(this.toJsonString(), JsonObject.class);
	}

	public String toJsonString() {
		if (gson == null) {
			gson = new Gson();
		}
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
		if (gson == null) {
			gson = new Gson();
		}
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
