package at.tuwien.ict.acona.cell.datastructures;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils;

public class JsonRpcResponse {
	private transient Gson gson = new Gson();
	private transient GsonUtils util = new GsonUtils();

	private final String jsonrpc;
	private final String id;
	private JsonElement result;
	private JsonRpcError error;

	public JsonRpcResponse(JsonRpcRequest request, JsonElement result) {
		super();
		this.id = request.getId();
		this.jsonrpc = request.getJsonrpc();
		this.result = result;
		this.error = null;
	}

	public JsonRpcResponse(JsonRpcRequest request, JsonRpcError error) {
		super();
		this.id = request.getId();
		this.jsonrpc = request.getJsonrpc();
		this.result = null;
		this.error = error;
	}

	public JsonRpcResponse(JsonRpcRequest request, List<?> result) throws Exception {
		super();
		this.id = request.getId();
		this.jsonrpc = request.getJsonrpc();
		this.setResult(result);
		this.error = null;
	}

	public String getId() {
		return id;
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

	public JsonRpcError getError() {
		return error;
	}

	public boolean hasError() {
		return (this.error != null ? true : false);
	}

	public void setError(JsonRpcError error) {
		this.error = error;
	}

	public JsonObject toJson() {
		JsonObject req = new JsonObject();
		req.addProperty("id", id);
		req.addProperty("jsonrpc", jsonrpc);
		req.add("result", result);
		if (this.error != null) {
			req.add("error", this.error.toJson());
		} else {
			req.add("error", null);
		}

		return req;
	}

	//	private <T> List<T> convertJsonArrayToList(JsonArray jsonArray, TypeToken<T> token) {
	//		List<T> result = gson.fromJson(jsonArray.toString(), token.getType());
	//		return result;
	//	}

	public <T> T getResult(TypeToken<T> token) throws Exception {
		T result = null;
		//if (this.getResult().isJsonArray()) {
		if (gson == null) {
			gson = new Gson();
		}

		try {
			result = gson.fromJson(this.getResult(), token.getType());
		} catch (JsonSyntaxException e) {
			throw new JsonSyntaxException("Cannot convert " + this.getResult() + " to datatype " + token, e);
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonRpcResponse: jsonrpc=");
		builder.append(jsonrpc);
		builder.append(", id=");
		builder.append(id);
		builder.append(", result=");
		builder.append(result);
		builder.append(", error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}
}
