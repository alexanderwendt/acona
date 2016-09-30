package at.tuwien.ict.acona.cell.config;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CellFunctionConfig {
	public static final String CELLFUNCTIONNAME = "functionname";
	public static final String CELLFUNCTIONCLASS = "functionclass";
	public static final String CELLSUBSCRIPTIONS = "subscriptions";
	public static final String CELLEXECUTERATE = "executerate";
	public static final String CELLEXECUTEONCE = "executeonce";
	
	private final JsonObject configObject;
	
	public static CellFunctionConfig newConfig(String name, String className) {
		return new CellFunctionConfig(name, className);
	}
	
	public static CellFunctionConfig newConfig(String name, Class<?> clzz) {
		String className = clzz.getName();
		return new CellFunctionConfig(name, className);
	}
	
	/**
	 * Config, where the function name is the class name for a simple class + hashcode
	 * 
	 * @param clzz
	 * @return
	 */
	public static CellFunctionConfig newConfig(Class<?> clzz) {
		String className = clzz.getName();
		return new CellFunctionConfig(clzz.getSimpleName() + className.hashCode(), className);
	}
	
	public static CellFunctionConfig newConfig(JsonObject config) {
		return new CellFunctionConfig(config);
	}
	
	private CellFunctionConfig(String name, String className) {
		this.configObject= new JsonObject();
		this.configObject.add(CELLSUBSCRIPTIONS, new JsonArray());
		this.setName(name).setClassName(className);
	}
	
	private CellFunctionConfig(JsonObject config) {
		this.configObject = config;
	}
	
	private CellFunctionConfig setName(String name) {
		this.configObject.addProperty(CELLFUNCTIONNAME, name);
		return this;
	}
	
	private CellFunctionConfig setClassName(String className) {
		this.configObject.addProperty(CELLFUNCTIONCLASS, className);
		return this;
	}
	
	public CellFunctionConfig setExecuterate(int rateInMs) {
		this.configObject.addProperty(CELLEXECUTERATE, rateInMs);
		return this;
	}
	
	public CellFunctionConfig setExecuteOnce(boolean isExecuteOnce) {
		this.configObject.addProperty(CELLEXECUTEONCE, isExecuteOnce);
		return this;
	}
	
	public CellFunctionConfig addSubscription(DatapointConfig config) {
		this.configObject.getAsJsonArray(CELLSUBSCRIPTIONS).add(config.toJsonObject());
		return this;
	}
	
	public CellFunctionConfig setProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}
	
	public CellFunctionConfig setProperty(String name, JsonObject value) {
		this.configObject.add(name, value);
		return this;
	}
	
	public String getName() {
		return this.configObject.getAsJsonPrimitive(CELLFUNCTIONNAME).getAsString();
	}
	
	public String getClassName() {
		return this.configObject.getAsJsonPrimitive(CELLFUNCTIONCLASS).getAsString();
	}
	
	public JsonPrimitive isExecuteOnce() {
		return this.configObject.getAsJsonPrimitive(CELLEXECUTEONCE);
	}
	
	public JsonPrimitive getExecuteRate() {
		return this.configObject.getAsJsonPrimitive(CELLEXECUTERATE);
	}
	
	public List<DatapointConfig> getSubscriptionConfig() {
		JsonArray array = this.configObject.getAsJsonArray(CELLSUBSCRIPTIONS);
		//Gson gson = new Gson();
		//Type type = new TypeToken<List<SubscriptionConfig>>(){}.getType();
		//List<SubscriptionConfig> configList = gson.fromJson(array, type);
		List<DatapointConfig> result = new ArrayList<DatapointConfig>();
		array.forEach(a->{
			
			try {
				result.add(DatapointConfig.newConfig(a.getAsJsonObject()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		return result;
	}
	
	public String getProperty(String key) {
		return this.configObject.getAsJsonPrimitive(key).getAsString();
	}
	
	public JsonObject getPropertyAsJsonObject(String key) {
		return this.configObject.getAsJsonObject(key);
	}
	
	public JsonObject toJsonObject() {
		return this.configObject;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CellFunctionConfig [configObject=");
		builder.append(configObject);
		builder.append("]");
		return builder.toString();
	}
	
}
