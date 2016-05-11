package at.tuwien.ict.acona.cell.config;

import com.google.gson.JsonObject;

public class BehaviourConfig {
	private static final String BEHAVIOURNAME = "behaviourname";
	private static final String BEHAVIOURCLASS = "behaviourclass";
	
	private final JsonObject configObject = new JsonObject();
	
	public static BehaviourConfig newConfig(String name, String className) {
		return new BehaviourConfig(name, className);
	}
	
	private BehaviourConfig(String name, String className) {
		this.setName(name).setClassName(className);
	}
	
	private BehaviourConfig setName(String name) {
		this.configObject.addProperty(BEHAVIOURNAME, name);
		return this;
	}
	
	private BehaviourConfig setClassName(String className) {
		this.configObject.addProperty(BEHAVIOURCLASS, className);
		return this;
	}
	
	public BehaviourConfig setProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}
	
	public String getName() {
		return this.configObject.getAsJsonPrimitive(BEHAVIOURNAME).getAsString();
	}
	
	public String getClassName() {
		return this.configObject.getAsJsonPrimitive(BEHAVIOURCLASS).getAsString();
	}
	
	public String getProperty(String key) {
		return this.configObject.getAsJsonPrimitive(key).getAsString();
	}
	
	public JsonObject toJsonObject() {
		return this.configObject;
	}
}
