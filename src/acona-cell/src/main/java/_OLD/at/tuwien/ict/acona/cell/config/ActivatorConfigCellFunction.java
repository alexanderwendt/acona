package _OLD.at.tuwien.ict.acona.cell.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ActivatorConfigCellFunction {
	private static final String CELLFUNCTIONNAME = "cellfunctionname";
	private static final String CELLFUNCTIONMAP = "cellfunctionmap";	//id to datapoint to subscribe
	
	private final JsonObject configObject = new JsonObject();
	
	public static ActivatorConfigCellFunction newConfig(String name) {
		return new ActivatorConfigCellFunction(name);
	}
	
	private ActivatorConfigCellFunction(String name) {
		//Create the map
		this.configObject.add(CELLFUNCTIONMAP, new JsonArray());
		this.setName(name);
	}
	
	private ActivatorConfigCellFunction setName(String name) {
		this.configObject.addProperty(CELLFUNCTIONNAME, name);
		return this;
	}
	
	public ActivatorConfigCellFunction addMapping(String id, String datapointaddress) {
		JsonObject object = new JsonObject();
		object.addProperty(id, datapointaddress);
		this.configObject.getAsJsonArray(CELLFUNCTIONMAP).add(object);
		return this;
	}
	
	public ActivatorConfigCellFunction setProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}
	
	public String getName() {
		return this.configObject.getAsJsonPrimitive(CELLFUNCTIONNAME).getAsString();
	}
	
	public String getProperty(String key) {
		return this.configObject.getAsJsonPrimitive(key).getAsString();
	}
	
	public JsonArray getMapping() {
		return this.configObject.getAsJsonArray(CELLFUNCTIONMAP);
	}
	
	public JsonObject toJsonObject() {
		return this.configObject;
	}
}
