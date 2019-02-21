package at.tuwien.ict.acona.cell.datastructures;

import com.google.gson.JsonObject;

public class JsonRpcError {

	private String name;
	private int code;
	private String message;
	private String error;

	public JsonRpcError(String name, int code, String message, String error) {
		super();
		this.name = name;
		this.code = code;
		this.message = message;
		this.error = error;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public JsonObject toJson() {
		JsonObject req = new JsonObject();
		req.addProperty("name", name);
		req.addProperty("code", code);
		req.addProperty("message", message);
		req.addProperty("error", error);

		return req;

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonRpcError [name=");
		builder.append(name);
		builder.append(", code=");
		builder.append(code);
		builder.append(", message=");
		builder.append(message);
		builder.append(", error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}

}
