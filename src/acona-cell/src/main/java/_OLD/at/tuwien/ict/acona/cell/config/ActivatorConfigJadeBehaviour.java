package _OLD.at.tuwien.ict.acona.cell.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ActivatorConfigJadeBehaviour {
	private static final String ACTIVATORNAME = "activatorname";
	private static final String ACTIVATORMAP = "activatormap";
	private static final String ACTIVATORBEHAVIOUR = "activatorbehaviour";
	private static final String ACTIVATORLOGIC = "activatorlogic";
	
	private final JsonObject configObject = new JsonObject();
	
	public static ActivatorConfigJadeBehaviour newConfig(String name) {
		return new ActivatorConfigJadeBehaviour(name);
	}
	
	private ActivatorConfigJadeBehaviour(String name) {
		//Create the map
		this.configObject.add(ACTIVATORMAP, new JsonArray());
		this.setName(name);
	}
	
	private ActivatorConfigJadeBehaviour setName(String name) {
		this.configObject.addProperty(ACTIVATORNAME, name);
		return this;
	}
	
	public ActivatorConfigJadeBehaviour addMapping(String datapointaddress, String conditionName) {
		JsonObject object = new JsonObject();
		object.addProperty(datapointaddress, conditionName);
		this.configObject.getAsJsonArray(ACTIVATORMAP).add(object);
		return this;
	}
	
	public ActivatorConfigJadeBehaviour setBehaviour(String behaviour) {
		this.configObject.addProperty(ACTIVATORBEHAVIOUR, behaviour);
		return this;
	}
	
	public ActivatorConfigJadeBehaviour setActivatorLogic(String logic) {
		this.configObject.addProperty(ACTIVATORLOGIC, logic);
		return this;
	}
	
	public ActivatorConfigJadeBehaviour setProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}
	
	public String getName() {
		return this.configObject.getAsJsonPrimitive(ACTIVATORNAME).getAsString();
	}
	
	public String getProperty(String key) {
		return this.configObject.getAsJsonPrimitive(key).getAsString();
	}
	
	public String getActivationLogic() {
		return this.configObject.getAsJsonPrimitive(ACTIVATORLOGIC).getAsString();
	}
	
	public String getActivationBehaviour() {
		return this.configObject.getAsJsonPrimitive(ACTIVATORBEHAVIOUR).getAsString();
	}
	
	public JsonArray getMapping() {
		return this.configObject.getAsJsonArray(ACTIVATORMAP);
	}
	
	public JsonObject toJsonObject() {
		return this.configObject;
	}
}
