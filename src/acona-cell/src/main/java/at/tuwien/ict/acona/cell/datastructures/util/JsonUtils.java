package at.tuwien.ict.acona.cell.datastructures.util;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtils {
	private final Gson gson = new Gson();

	public JsonUtils() {

	}

	public boolean isJSONValid(String JSON_STRING) {
		try {
			gson.fromJson(JSON_STRING, JsonObject.class);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public JsonObject generateErrorMessage(String name, int code, String message, String error) {
		// "error": {"name": "JSONRPCError",
		// "code": (number 100-999),
		// "message": "Some Error Occurred",
		// "error": "whatever you want\n(a traceback?)"

		JsonObject errorObject = new JsonObject();
		errorObject.addProperty("name", name);
		errorObject.addProperty("code", code);
		errorObject.addProperty("message", message);
		errorObject.addProperty("error", error);

		return errorObject;
	}

	public JsonObject generateJsonRpcRequest(String methodName, List<Object> arg) {
		Object[] objectArray = new Object[arg.size()];
		objectArray = arg.toArray(objectArray);

		return this.generateJsonRpcRequest(methodName, objectArray);
	}

	/**
	 * Generate a JSON-RPC request based on inputs
	 * 
	 * @param methodName
	 * @param id
	 * @param args
	 * @return
	 */
	public JsonObject generateJsonRpcRequest(String methodName, Object... args) {
		JsonObject req = new JsonObject();
		req.addProperty("id", req.hashCode());
		req.addProperty("jsonrpc", "2.0");
		req.addProperty("method", methodName);

		JsonArray params = new JsonArray();
		for (Object o : args) {
			params.add(gson.toJsonTree(o));
		}

		req.add("params", params);

		return req;
	}

	/**
	 * Generate JSON response
	 * 
	 * @param requestid
	 * @param result
	 * @param error
	 * @return
	 */
	public JsonObject generateJsonRpcResponse(JsonObject request, Object result, String error) {
		JsonObject response = new JsonObject();
		response.addProperty("id", request.getAsJsonPrimitive("id").getAsString());
		response.addProperty("jsonrpc", "2.0");

		if (error == null || error.isEmpty()) {
			response.add("result", null);
			response.add("error", new JsonPrimitive(error));
		} else {
			response.add("result", gson.toJsonTree(result));
			response.add("error", null);
		}

		return response;
	}

	/**
	 * Generate JSON response
	 * 
	 * @param requestid
	 * @param result
	 * @param error
	 * @return
	 */
	public JsonObject generateJsonRpcResponse(String requestid, Object result, String error) {
		JsonObject response = new JsonObject();
		response.addProperty("id", requestid);
		response.addProperty("jsonrpc", "2.0");

		if (error == null || error.isEmpty()) {
			response.add("result", null);
			response.add("error", new JsonPrimitive(error));
		} else {
			response.add("result", gson.toJsonTree(result));
			response.add("error", null);
		}

		return response;
	}

	/**
	 * Generate Json Notify
	 * 
	 * @param methodName
	 * @param args
	 * @return
	 */
	public JsonObject generateJsonRpcNotify(String methodName, Object... args) {
		return this.generateJsonRpcRequest(methodName, null, args);
	}
}
