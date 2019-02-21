package at.tuwien.ict.acona.cell.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DatapointBuilder {

	public final static String KEYADDRESS = "ADDRESS";
	public final static String KEYAGENT = "AGENT";
	public final static String KEYVALUE = "VALUE";

	private final static String NULLADDRESS = "NULLDATAPOINT";

	private static final Gson gson = new Gson();
	// private final JsonObject jsondatapoint;

	private final static Logger log = LoggerFactory.getLogger(DatapointBuilder.class);

	public synchronized static Datapoint toDatapoint(String data) throws Exception {
		log.debug("Datapoint to convert={}", data);
		JsonObject jsonData = gson.fromJson(data, JsonObject.class);
		return DatapointBuilder.toDatapoint(jsonData);
	}

	public synchronized static boolean isDatapoint(JsonObject data) {
		boolean result = false;

		if (data.has(DatapointBuilder.KEYADDRESS) && data.has(DatapointBuilder.KEYVALUE)) {
			result = true;
		}

		return result;
	}

	public synchronized static Datapoint newNullDatapoint() {
		return new Datapoint(NULLADDRESS);
	}

	public synchronized static Datapoint newDatapoint(String address) {
		return new Datapoint(address);
	}

	public synchronized static Datapoint toDatapoint(JsonObject data) throws IllegalArgumentException {
		Datapoint result = null;

		try {
			if (DatapointBuilder.isDatapoint(data) == true) {
				result = DatapointBuilder.newDatapoint(data.get(KEYADDRESS).getAsString()).setValue(data.get(KEYVALUE));
				// .setType(data.get(KEYTYPE).getAsString()).setValue(data.get(KEYVALUE));
			} else {
				throw new IllegalArgumentException("Cannot cast json data to datapoint " + data);
			}

		} catch (IllegalArgumentException e) {
			throw e;
		}

		return result;
	}

	public synchronized static boolean isNullDatapoint(JsonObject data) {
		boolean result = false;

		if (data.has(KEYADDRESS) && data.get(KEYADDRESS).getAsString().equals(NULLADDRESS)) {
			result = true;
		}

		return result;
	}
}
