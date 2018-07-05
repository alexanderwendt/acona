package at.tuwien.ict.acona.cognitivearchitecture.datastructures;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public abstract class BasicDataStructure {

	private String name;
	private String type;
	// private String id;

	public BasicDataStructure(String name, String type) {
		super();
		this.name = name;
		this.type = type;

		// this.id = this.name + this.as
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public boolean isNull() {
		boolean result = false;
		if (this.name == null || this.name.isEmpty()) {
			result = true;
		}

		return result;
	}

	public JsonObject toJsonObject() {
		Gson gson = new Gson();

		return gson.fromJson(gson.toJson(this), JsonObject.class);
	}
//	public void setType(String type) {
//		this.type = type;
//	}

//	public String getId() {
//		return id;
//	}
//	
//	public void setId(String id) {
//		this.id = id;
//	}
}
