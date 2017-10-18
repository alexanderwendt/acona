package at.tuwien.ict.acona.cell.datastructures;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Datapoint {
	//public final static String KEYADDRESS = "ADDRESS";
	//public final static String KEYAGENT = "AGENT";
	//public final static String KEYVALUE = "VALUE";

	//TODO: Create datapoints that can take Chunks and Chunk arrays and Json arrays

	private String ADDRESS = "";
	private String AGENT = "";
	private JsonElement VALUE = new JsonObject(); // new JsonObject();

	private transient Gson gson = new Gson(); //Add transient not to serialize this

	//private final static Logger log = LoggerFactory.getLogger(Datapoint.class);

	/**
	 * Create a datapoint from an address
	 * 
	 * @param address:
	 *            The following syntax can be used: x.x for local datapoints,
	 *            [agent]:[localaddress] for global datapoints
	 */
	public Datapoint(String address) {
		this.ADDRESS = this.getLocalAddressFromString(address);
		this.AGENT = this.getAgentNameFromString(address);
	}

	public Datapoint(String agent, String address) {
		this.ADDRESS = this.getLocalAddressFromString(address);
		this.AGENT = agent;
	}

	private String getAgentNameFromString(String address) {
		String result = "";
		if (address.contains(":")) {
			result = address.split(":")[0];
		}

		return result;
	}

	private String getLocalAddressFromString(String address) {
		String result = address;
		if (address.contains(":")) {
			result = address.split(":")[1];
		}

		return result;
	}

	private String combineAddress(String agent, String address) {
		String combinedAddress = address;
		if (agent.isEmpty() == false) {
			combinedAddress = agent + ":" + address;
		}

		return combinedAddress;
	}

	public String getAddress() {
		return ADDRESS;
	}

	public String getCompleteAddress() {
		return this.combineAddress(this.AGENT, this.ADDRESS);
	}

	public String getAgent() {
		return this.AGENT;
	}

	public String getAgent(String defaultValue) {
		String result = defaultValue;
		if (this.AGENT.isEmpty() == false) {
			result = this.AGENT;
		}

		return result;
	}

	public void setAgent(String agent) {
		this.AGENT = agent;
	}

	/**
	 * Set an agent if no agent has been set
	 * 
	 * @param agent
	 */
	public void setAgentIfAbsent(String agent) {
		if (this.hasLocalAgent() == true) {
			this.AGENT = agent;
		}
	}

	public boolean hasLocalAgent() {
		boolean result = false;
		if (this.AGENT == "") {
			result = true;
		}

		return result;
	}

	public void removeAgent() {
		this.AGENT = "";
	}

	public JsonElement getValue() {
		return VALUE;
	}

	public <T> T getValue(Class<T> clzz) {
		if (gson == null) {
			gson = new Gson();
		}

		T result = null;
		if (this.getValue().isJsonNull() == false) {
			result = gson.fromJson(this.VALUE, clzz);
		}

		return result;
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

	public boolean hasEmptyValue() {
		boolean result = false;
		if (VALUE.toString().equals("{}") == true) {
			result = true;
		}

		return result;
	}

	public Datapoint setValue(String value) {
		this.VALUE = new JsonPrimitive(value);

		return this;
	}

	public Datapoint setValue(JsonElement value) {
		this.VALUE = value;

		return this;
	}

	public Datapoint setValue(boolean value) {
		this.VALUE = new JsonPrimitive(value);

		return this;
	}

	public Datapoint setValue(double value) {
		this.VALUE = new JsonPrimitive(value);

		return this;
	}

	public Datapoint setValue(int value) {
		this.VALUE = new JsonPrimitive(value);

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
