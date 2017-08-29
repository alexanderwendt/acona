package at.tuwien.ict.acona.cell.datastructures;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils;

public class JsonRpcRequest {

	private transient Gson gson = new Gson();
	private transient GsonUtils util = new GsonUtils();

	private final String jsonrpc;
	private String method;
	private final String id;
	private Object[] params;

	public JsonRpcRequest(String method, boolean isNotification, Object[] params) {
		super();
		this.method = method;
		this.jsonrpc = "2.0";
		if (isNotification == false) {
			this.id = String.valueOf(this.hashCode());
		} else {
			this.id = null;
		}

		this.params = params;
	}

	public JsonRpcRequest(String method, int numberOfParameter) {
		super();
		this.method = method;
		this.jsonrpc = "2.0";
		this.id = String.valueOf(this.hashCode());

		this.params = new Object[numberOfParameter];
	}

	public JsonRpcRequest(String stringrpcRequest) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}

		JsonRpcRequest rpcRequest2 = null;

		try {
			rpcRequest2 = gson.fromJson(stringrpcRequest, JsonRpcRequest.class);
		} catch (Exception e) {
			throw new Exception("Cannot convert string into request. String=" + stringrpcRequest, e);
		}

		this.method = rpcRequest2.getMethod();
		this.id = rpcRequest2.getId();
		this.jsonrpc = rpcRequest2.getJsonrpc();
		this.params = rpcRequest2.getParams();
	}

	public JsonRpcRequest(JsonObject rpcRequest) {
		if (gson == null) {
			gson = new Gson();
		}
		this.method = rpcRequest.getAsJsonPrimitive("method").getAsString();
		this.id = rpcRequest.getAsJsonPrimitive("id").getAsString();
		this.jsonrpc = rpcRequest.getAsJsonPrimitive("jsonprc").getAsString();

		JsonArray params = rpcRequest.getAsJsonArray("params");

		Object[] objectArray = gson.fromJson(params, Object[].class);
		this.params = objectArray;
	}

	public void setParameters(Object... obj) {
		this.params = obj;
	}

	/**
	 * Get a certain parameter from the parameters if it is a list
	 * 
	 * @param index
	 * @param type
	 * @return
	 */
	public <T> T getParameter(int index, TypeToken<T> type) throws JsonSyntaxException {
		T result = null;

		//Get first parameter and convert it
		if (gson == null) {
			gson = new Gson();
		}

		try {
			//First convert to JsonObject
			JsonElement element = gson.toJsonTree(this.getParams()[index]);
			result = gson.fromJson(element, type.getType());
		} catch (JsonSyntaxException e) {
			throw new JsonSyntaxException("Cannot convert " + this.getParams()[index] + " to datatype " + type, e);
		}

		return result;
	}

	public void setParameterAsList(int index, List<?> obj) throws Exception {
		if (util == null) {
			util = new GsonUtils();
		}
		JsonArray array = util.convertListToJsonArray(obj);
		if (this.params != null || this.params.length > index) {
			this.params[index] = array;
		} else {
			throw new NullPointerException("Parameter list is null");
		}
	}

	public JsonRpcRequest setParameterAsValue(int index, String value) throws Exception {
		return setParameterAsValue(index, new JsonPrimitive(value));
	}

	public JsonRpcRequest setParameterAsValue(int index, int value) throws Exception {
		return setParameterAsValue(index, new JsonPrimitive(value));
	}

	public JsonRpcRequest setParameterAsValue(int index, double value) throws Exception {
		return setParameterAsValue(index, new JsonPrimitive(value));
	}

	public JsonRpcRequest setParameterAsValue(int index, boolean value) throws Exception {
		return setParameterAsValue(index, new JsonPrimitive(value));
	}

	public JsonRpcRequest setParameterAsValue(int index, JsonElement obj) throws Exception {
		if (gson == null) {
			gson = new Gson();
		}
		//JsonElement jsonObject = gson.toJsonTree(obj, T);

		if (this.params != null || this.params.length > index) {
			this.params[index] = obj.toString();
		} else {
			throw new NullPointerException("Parameter list is null");
		}

		return this;
	}

	/**
	 * Get a certain parameter from the parameter list if not an "inside" type
	 * 
	 * @param index
	 * @param clazz
	 * @return
	 */
	public <T> T getParameter(int index, Class<T> clazz) {
		//Get first parameter and convert it
		if (gson == null) {
			gson = new Gson();
		}
		T result = gson.fromJson(this.getParams()[index].toString(), clazz);
		return result;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getId() {
		return id;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public JsonObject toJson() {
		if (gson == null) {
			gson = new Gson();
		}
		JsonObject req = new JsonObject();
		req.addProperty("id", id);
		req.addProperty("jsonrpc", jsonrpc);
		req.addProperty("method", method);

		JsonArray paramsArray = new JsonArray();
		for (Object o : params) {
			paramsArray.add(gson.toJsonTree(o));
		}

		req.add("params", paramsArray);

		return req;
	}

	public String getJsonrpc() {
		return jsonrpc;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("jsonrpc=");
		builder.append(jsonrpc);
		builder.append(", method=");
		builder.append(method);
		builder.append(", id=");
		builder.append(id);
		builder.append(", params=");
		builder.append(Arrays.toString(params));
		return builder.toString();
	}

}
