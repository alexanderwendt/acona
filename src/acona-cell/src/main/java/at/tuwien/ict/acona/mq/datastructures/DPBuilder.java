package at.tuwien.ict.acona.mq.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DPBuilder {

	public final static String KEYADDRESS = "ADDRESS";
	public final static String KEYAGENT = "AGENT";
	public final static String KEYVALUE = "VALUE";

	private final static String NULLADDRESS = "NULLDATAPOINT";

	private final Gson gson = new Gson();
	// private final JsonObject jsondatapoint;

	private final static Logger log = LoggerFactory.getLogger(DPBuilder.class);

	public DPBuilder() {

	}

	public Datapoint toDatapoint(String data) throws Exception {
		log.debug("Datapoint to convert={}", data);
		JsonObject jsonData = gson.fromJson(data, JsonObject.class);
		return this.toDatapoint(jsonData);
	}

	public boolean isDatapoint(JsonObject data) {
		boolean result = false;

		if (data.has(DPBuilder.KEYADDRESS) && data.has(DPBuilder.KEYVALUE)) {
			result = true;
		}

		return result;
	}

	public Datapoint newNullDatapoint() {
		return new Datapoint(NULLADDRESS);
	}

	public Datapoint newDatapoint(String address) {
		return new Datapoint(address);
	}

	public Datapoint toDatapoint(JsonObject data) throws IllegalArgumentException {
		Datapoint result = null;

		try {
			if (this.isDatapoint(data) == true) {
				result = this.newDatapoint(data.get(KEYADDRESS).getAsString()).setValue(data.get(KEYVALUE));
				// .setType(data.get(KEYTYPE).getAsString()).setValue(data.get(KEYVALUE));
			} else {
				throw new IllegalArgumentException("Cannot cast json data to datapoint " + data);
			}

		} catch (IllegalArgumentException e) {
			throw e;
		}

		return result;
	}

	public boolean isNullDatapoint(JsonObject data) {
		boolean result = false;

		if (data.has(KEYADDRESS) && data.get(KEYADDRESS).getAsString().equals(NULLADDRESS)) {
			result = true;
		}

		return result;
	}
}
