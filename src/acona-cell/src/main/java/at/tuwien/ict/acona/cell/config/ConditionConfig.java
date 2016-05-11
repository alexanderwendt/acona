package at.tuwien.ict.acona.cell.config;

import com.google.gson.JsonObject;

public class ConditionConfig {
	private static final String CONDITIONNAME = "conditionname";
	private static final String CONDITIONCLASS = "conditionclass";
	
	private final JsonObject conditionConfig = new JsonObject();
	
	public static ConditionConfig newConfig(String name, String className) {
		return new ConditionConfig(name, className);
	}
	
	private ConditionConfig(String name, String className) {
		this.setName(name).setClassName(className);
	}
	
	private ConditionConfig setName(String name) {
		this.conditionConfig.addProperty(CONDITIONNAME, name);
		return this;
	}
	
	private ConditionConfig setClassName(String className) {
		this.conditionConfig.addProperty(CONDITIONCLASS, className);
		return this;
	}
	
	public ConditionConfig setProperty(String name, String value) {
		this.conditionConfig.addProperty(name, value);
		return this;
	}
	
	public String getName() {
		return this.conditionConfig.getAsJsonPrimitive(CONDITIONNAME).getAsString();
	}
	
	public String getClassName() {
		return this.conditionConfig.getAsJsonPrimitive(CONDITIONCLASS).getAsString();
	}
	
	public String getProperty(String key) {
		return this.conditionConfig.getAsJsonPrimitive(key).getAsString();
	}
	
	public JsonObject toJsonObject() {
		return this.conditionConfig;
	}
}
