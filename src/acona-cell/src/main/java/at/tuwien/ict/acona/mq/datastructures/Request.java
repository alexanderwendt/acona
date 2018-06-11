package at.tuwien.ict.acona.mq.datastructures;

import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils;

public class Request {

	public final static String CORRELATIONID = "correlationid";
	public final static String REPLYTO = "replyto";
	public final static String PARAMETER = "parameter";

	private transient GsonUtils util = new GsonUtils();

	private final String correlationid;
	private final String replyto;
	private JsonObject parameter;

	// private transient Gson gson = new Gson();
	// private transient GsonUtils util = new GsonUtils();

	// private String replyto; //Reply-to
	// private final String correlationid;
	// private JsonObject params;

	/**
	 * @param address
	 * @param replyToTopic
	 */
	public Request(String replyToTopic) {
		// Add correlationid
		correlationid = UUID.randomUUID().toString();
		// parameter.addProperty(CORRELATIONID, correlationid);

		// Add ReplyTo
		replyto = replyToTopic;
		// parameter.addProperty(REPLYTO, replyToTopic);
	}

	/**
	 * @param rpcRequest
	 * @throws Exception
	 */
	public Request(JsonObject obj) throws Exception {
		if (isRequest(obj) == true) {
			this.correlationid = obj.get(CORRELATIONID).getAsString();
			this.parameter = obj.get(PARAMETER).getAsJsonObject();
			this.replyto = obj.get(REPLYTO).getAsString();
		} else {
			throw new Exception("No Request");
		}

	}

	public JsonObject toJson() {
		if (util == null) {
			util = new GsonUtils();
		}

		JsonObject result = util.getGson().toJsonTree(this).getAsJsonObject();

		return result;
	}

	public static boolean isRequest(JsonObject obj) {
		boolean result = false;

		if (obj.has(CORRELATIONID) && obj.has(REPLYTO) && obj.has(PARAMETER)) {
			result = true;
		}

		return result;
	}

	/**
	 * Add a parameter as JsonElement
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Request setParameter(String key, JsonElement value) {
		this.parameter.add(key, value);
		return this;
	}

	public Request setParameter(String key, String value) {
		this.parameter.addProperty(key, value);
		return this;
	}

	public Request setParameter(String key, boolean value) {
		this.parameter.addProperty(key, value);
		return this;
	}

	public Request setParameter(String key, int value) {
		this.parameter.addProperty(key, value);
		return this;
	}

	public Request setParameter(String key, double value) {
		this.parameter.addProperty(key, value);
		return this;
	}

	public <T> Request setParameter(String key, List<T> value) throws Exception {
		// Convert list to JsonElement
		JsonArray array = this.util.convertListToJsonArray(value);

		this.parameter.add(key, array);

		return this;
	}

	public String getCorrelationId() {
		return this.correlationid;
	}

	public String getReplyTo() {
		return this.replyto;
	}

//	/**
//	 * Convert any list with jsonconvertable objects to jsonarray
//	 * 
//	 * @param list
//	 * @return
//	 * @throws Exception
//	 */
//	private JsonArray convertListToJsonArray(List<?> list) throws Exception {
//		if (this.util == null) {
//			this.util = new GsonUtils();
//		}
//
//		JsonElement element = util.getGson().toJsonTree(list, new TypeToken<List<?>>() {}.getType());
//
//		if (!element.isJsonArray()) {
//			// fail appropriately
//			throw new Exception("Element is no JsonArray");
//		}
//
//		JsonArray jsonArray = element.getAsJsonArray();
//		return jsonArray;
//	}

	/**
	 * Get a certain parameter from the parameters if it is a list
	 * 
	 * @param index
	 * @param type
	 * @return
	 */
	public <T> T getParameter(String key, TypeToken<T> type) throws JsonSyntaxException {
		T result = null;

		// Get first parameter and convert it
		if (this.util == null) {
			this.util = new GsonUtils();
		}

		try {
			// First convert to JsonObject
			JsonElement element = util.getGson().toJsonTree(this.parameter.get(key));
			result = util.getGson().fromJson(element, type.getType());
		} catch (JsonSyntaxException e) {
			throw new JsonSyntaxException("Cannot convert " + this.parameter.get(key) + " to datatype " + type, e);
		}

		return result;
	}

	/**
	 * Get a certain parameter from the parameter list if not an "inside" type. All parameters must be json format
	 * 
	 * @param index
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public <T> T getParameter(String key, Class<T> clazz) throws Exception {
		T result = null;

		if (this.util == null) {
			this.util = new GsonUtils();
		}

		try {
			result = util.getGson().fromJson(this.parameter.get(key), clazz);
		} catch (Exception e) {
			throw new Exception("Parameter is not Json-Format. All parameters must be in Json format (e.g. JsonPrimitive()). Object=" + this.parameter.get(key), e);
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("|params=");
		builder.append(this.parameter);
		return builder.toString();
	}

}
