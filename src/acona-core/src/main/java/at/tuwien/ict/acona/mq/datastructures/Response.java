package at.tuwien.ict.acona.mq.datastructures;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.mq.utils.GsonUtils;

public class Response {
	public final static String CORRELATIONID = "correlationid";
	public final static String REPLYTO = "replyto";
	public final static String RESULT = "result";
	public final static String ERROR = "error";

	private transient GsonUtils util = new GsonUtils();

	private final String correlationid;
	private final String replyto;
	private JsonElement result;
	private RequestError error;

	public Response(Request request, JsonElement result) {
		this.correlationid = request.getCorrelationId();
		this.replyto = request.getReplyTo();

		this.result = result;
		this.error = null;
	}

	public Response(Request request) {
		this.correlationid = request.getCorrelationId();
		this.replyto = request.getReplyTo();

		this.result = null;
		this.error = null;
	}

	public Response(Request request, RequestError resultOrError) {
		this.correlationid = request.getCorrelationId();
		this.replyto = request.getReplyTo();
		this.result = null;
		this.error = resultOrError;

	}

	public Response(Request request, List<?> result) throws Exception {
		this.correlationid = request.getCorrelationId();
		this.replyto = request.getReplyTo();
		this.setResult(result);
		this.error = null;
	}

	public static Response newResponse(String input) {
		Gson gson = new Gson();
		return gson.fromJson(input, Response.class);
	}

	public static boolean isResponse(JsonObject obj) {
		boolean result = false;

		if (obj.has(CORRELATIONID) && obj.has(REPLYTO) && (obj.has(RESULT) || obj.has(ERROR))) {
			result = true;
		}

		return result;
	}

	public String getCorrelationid() {
		return correlationid;
	}

	public String getReplyTo() {
		return this.replyto;
	}

	public JsonElement getResult() {
		return result;
	}

	public void setResult(JsonElement result) {
		this.result = result;
	}

	public void setResult(List<?> result) throws Exception {
		if (util == null) {
			util = new GsonUtils();
		}
		this.result = util.convertListToJsonArray(result);
	}

	public RequestError getError() {
		return error;
	}

	public boolean hasError() {
		return (this.error != null ? true : false);
	}

	public JsonObject toJson() {
		if (util == null) {
			util = new GsonUtils();
		}

		JsonObject result = util.getGson().toJsonTree(this).getAsJsonObject();

		return result;
	}

	// private <T> List<T> convertJsonArrayToList(JsonArray jsonArray, TypeToken<T> token) {
	// List<T> result = gson.fromJson(jsonArray.toString(), token.getType());
	// return result;
	// }

	public <T> T getResult(TypeToken<T> token) throws Exception {
		T result = null;
		// if (this.getResult().isJsonArray()) {
		if (util == null) {
			util = new GsonUtils();
		}

		try {
			result = util.getGson().fromJson(this.getResult(), token.getType());
		} catch (JsonSyntaxException e) {
			throw new JsonSyntaxException("Cannot convert " + this.getResult() + " to datatype " + token, e);
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("replyto=");
		builder.append(this.replyto);
		builder.append("|corrid=");
		builder.append(correlationid);
		builder.append("|result=");
		builder.append(result);
		builder.append("|error=");
		builder.append(error);
		return builder.toString();
	}

	public Response setError(String error) {
		this.error = new RequestError(error);
		return this;
	}

	/**
	 * Set the OK result as an achknowledge
	 */
	public Response setResultOK() {
		this.setResult(new JsonPrimitive("OK"));
		return this;
	}
}
