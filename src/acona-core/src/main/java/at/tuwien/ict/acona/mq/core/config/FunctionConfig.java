package at.tuwien.ict.acona.mq.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;

public class FunctionConfig {
	public static final String CELLFUNCTIONNAME = "functionname";
	public static final String CELLFUNCTIONCLASS = "functionclass";
	public static final String HOSTADDRESS = "host";
	public static final String USER= "user";
	public static final String PASSWORD = "password";
	public static final String CELLMANAGEDDATAPOINTS = "syncdatapoints";
	public static final String CELLEXECUTERATE = "executerate";
	public static final String CELLEXECUTEONCE = "executeonce";
	public static final String CELLFINISHSTATEAFTERSINGLERUN = "finishstatftersinglerun"; // This variable can only be set in the code
	public static final String GENERATERESPONDER = "generateresponder";
	//public static final String RESPONDERPROTOCOL = "responderprotocol";
	public static final String COMMUNICATORTIMEOUT = "communicatortimeout";
	// public static final String REGISTERSTATE = "registerstate";

	// private static Logger log = LoggerFactory.getLogger(CellFunctionConfig.class);

	/**
	 * 
	 */
	protected final JsonObject configObject;

	/**
	 * @param name
	 * @param className
	 * @return
	 */
	public static FunctionConfig newConfig(String name, String className) {
		return new FunctionConfig(name, className, new HashMap<String, Object>());
	}

	/**
	 * Create an empty cell function without setting parameters
	 * 
	 * @param name
	 * @param clzz
	 * @return
	 */
	public static FunctionConfig newConfig(String name, Class<?> clzz) {
		String className = clzz.getName();
		return new FunctionConfig(name, className, new HashMap<String, Object>());
	}
	
	/**
	 * Create a cell function with a map of parameters
	 * 
	 * @param name
	 * @param clzz
	 * @param params
	 * @return
	 */
	public static FunctionConfig newConfig(String name, Class<?> clzz, Map<String, Object> params) {
		String className = clzz.getName();
		return new FunctionConfig(name, className, params);
	}

	/**
	 * Config, where the function name is the class name for a simple class + hashcode
	 * 
	 * @param clzz
	 * @return
	 */
	public static FunctionConfig newConfig(Class<?> clzz) {
		String className = clzz.getName();
		return new FunctionConfig(clzz.getSimpleName() + className.hashCode(), className, new HashMap<String, Object>());
	}

	/**
	 * @param config
	 * @return
	 */
	public static FunctionConfig newConfig(JsonObject config) {
		return new FunctionConfig(config);
	}
	
	
	

	/**
	 * @param name
	 * @param className
	 */
	private FunctionConfig(String name, String className, Map<String, Object> params) {
		this.configObject = new JsonObject();
		this.configObject.add(CELLMANAGEDDATAPOINTS, new JsonArray());
		params.forEach((k, v)->this.configObject.addProperty(k, String.valueOf(v)));
		this.setName(name).setClassName(className);
	}

	/**
	 * @param config
	 */
	private FunctionConfig(JsonObject config) {
		this.configObject = config;
	}

	/**
	 * @param name
	 * @return
	 */
	private FunctionConfig setName(String name) {
		this.configObject.addProperty(CELLFUNCTIONNAME, name);
		return this;
	}

	// ======================//

	/**
	 * @return
	 */
	public String getName() {
		return this.configObject.getAsJsonPrimitive(CELLFUNCTIONNAME).getAsString();
	}

	/**
	 * @param className
	 * @return
	 */
	private FunctionConfig setClassName(String className) {
		this.configObject.addProperty(CELLFUNCTIONCLASS, className);
		return this;
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return this.configObject.getAsJsonPrimitive(CELLFUNCTIONCLASS).getAsString();
	}

	/**
	 * @param rateInMs
	 * @return
	 */
	public FunctionConfig setExecuterate(int rateInMs) {
		this.configObject.addProperty(CELLEXECUTERATE, rateInMs);
		return this;
	}

	// ======================//

	/**
	 * @return
	 */
	public JsonPrimitive getExecuteRate() {
		return this.configObject.getAsJsonPrimitive(CELLEXECUTERATE);
	}

	/**
	 * @param isExecuteOnce
	 * @return
	 */
	public FunctionConfig setExecuteOnce(boolean isExecuteOnce) {
		this.configObject.addProperty(CELLEXECUTEONCE, isExecuteOnce);
		return this;
	}

	// ======================//

	/**
	 * @return
	 */
	public JsonPrimitive isExecuteOnce() {
		return this.configObject.getAsJsonPrimitive(CELLEXECUTEONCE);
	}

	/**
	 * @param isExecuteOnce
	 * @return
	 */
	public FunctionConfig setFinishStateAfterSingleRun(boolean isFinishedAfterSingleRun) {
		this.configObject.addProperty(CELLFINISHSTATEAFTERSINGLERUN, isFinishedAfterSingleRun);
		return this;
	}

	/**
	 * @return
	 */
	public JsonPrimitive isFinishStateAfterSingleRun() {
		return this.configObject.getAsJsonPrimitive(CELLFINISHSTATEAFTERSINGLERUN);
	}

//	/**
//	 * @param isGenerateResponder
//	 * @return
//	 */
//	public CellFunctionConfig setGenerateReponder(boolean isGenerateResponder) {
//		this.configObject.addProperty(GENERATERESPONDER, isGenerateResponder);
//		return this;
//	}

	// ======================//

//	/**
//	 * @return
//	 */
//	public JsonPrimitive getGenerateReponder() {
//		return this.configObject.getAsJsonPrimitive(GENERATERESPONDER);
//	}

	/**
	 * @param responderProtocol
	 * @return
	 */
	public FunctionConfig setCommunicatorTimeout(int timeout) {
		this.configObject.addProperty(COMMUNICATORTIMEOUT, timeout);
		return this;
	}

	/**
	 * @return
	 */
	public JsonPrimitive getCommunicatorTimeout() {
		return this.configObject.getAsJsonPrimitive(COMMUNICATORTIMEOUT);
	}
	
	/**
	 * If no local host is used or other user and password than the standard data, use this method to set the connection.
	 * 
	 * @param host
	 * @return
	 */
	public FunctionConfig setHostData(String host, String user, String password) {
		this.configObject.addProperty(HOSTADDRESS, host);
		this.configObject.addProperty(USER, user);
		this.configObject.addProperty(PASSWORD, password);
		
		return this;
	}
	
	/**
	 * Get the MQTT host address
	 * 
	 * @return
	 */
	public JsonPrimitive getHost() {
		return this.configObject.getAsJsonPrimitive(HOSTADDRESS);
	}
	
	/**
	 * Get user name
	 * 
	 * @return
	 */
	public JsonPrimitive getUser() {
		return this.configObject.getAsJsonPrimitive(USER);
	}
	
	/**
	 * Get password
	 * 
	 * @return
	 */
	public JsonPrimitive getPassword() {
		return this.configObject.getAsJsonPrimitive(PASSWORD);
	}
	

	// ======================//

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public FunctionConfig setProperty(String name, String value) {
		if (this.configObject.has(name)) {
			this.configObject.remove(name);
		}
		
		this.configObject.addProperty(name, value);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public FunctionConfig setProperty(String name, int value) {
		if (this.configObject.has(name)) {
			this.configObject.remove(name);
		}
		
		this.configObject.addProperty(name, value);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public FunctionConfig setProperty(String name, double value) {
		if (this.configObject.has(name)) {
			this.configObject.remove(name);
		}
		
		this.configObject.addProperty(name, value);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public FunctionConfig setProperty(String name, boolean value) {
		if (this.configObject.has(name)) {
			this.configObject.remove(name);
		}
		
		this.configObject.addProperty(name, value);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public FunctionConfig setProperty(String name, JsonObject value) {
		if (this.configObject.has(name)) {
			this.configObject.remove(name);
		}
		
		this.configObject.add(name, value);
		return this;
	}

	// === Syncdatapoints ===//

	// =======================//

	// === Write datapoints ===//

	/**
	 * @param key
	 * @param type
	 * @return
	 */
	public <DATA_TYPE> DATA_TYPE getProperty(String key, Class<DATA_TYPE> type) {
		Gson gson = new Gson();

		return gson.fromJson(this.configObject.get(key), type);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 */
	public FunctionConfig setProperty(String key, Object value) {
		// TODO: Method not tested yet
		if (this.configObject.has(key)) {
			this.configObject.remove(key);
		}
		
		this.configObject.add(key, new Gson().toJsonTree(value));
		return this;
	}

	/**
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public String getProperty(String key) throws Exception {
		String result = "";
		try {
			result = this.configObject.getAsJsonPrimitive(key).getAsString();
		} catch (Exception e) {
			throw new Exception("Cannot find key=" + key + " in object=" + this.configObject + ". Please add the key to the configuration", e);
		}

		return result;
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getProperty(String key, String defaultValue) {
		String result = defaultValue;

		if (configObject.has(key)) {
			result = this.configObject.getAsJsonPrimitive(key).getAsString();
		}

		return result;
	}

	/**
	 * @param key
	 * @return
	 */
	public JsonObject getPropertyAsJsonObject(String key) {
		return this.configObject.getAsJsonObject(key);
	}

	// === Syncdatapoints ===//

	// =======================//

	// === Write datapoints ===//

	// === Syncdatapoints ===//

	/**
	 * @param type
	 * @return
	 */
	private List<DatapointConfig> getDatapointConfig(String type) {
		JsonArray array = this.configObject.getAsJsonArray(type);
		// Gson gson = new Gson();
		// Type type = new TypeToken<List<SubscriptionConfig>>(){}.getType();
		// List<SubscriptionConfig> configList = gson.fromJson(array, type);
		List<DatapointConfig> result = new ArrayList<>();
		array.forEach(a -> {

			try {
				result.add(DatapointConfig.newConfig(a.getAsJsonObject()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		return result;
	}

	/**
	 * @param config
	 * @return
	 */
	public FunctionConfig addManagedDatapoint(DatapointConfig config) {
		this.configObject.getAsJsonArray(CELLMANAGEDDATAPOINTS).add(config.toJsonObject());
		return this;
	}

	/**
	 * @param address
	 * @param mode
	 * @return
	 */
	public FunctionConfig addManagedDatapoint(String address, SyncMode mode) {
		return this.addManagedDatapoint(DatapointConfig.newConfig(address, address, mode));
	}

	/**
	 * @param id
	 * @param address
	 * @param agentId
	 * @param syncMode
	 * @return
	 */
	public FunctionConfig addManagedDatapoint(String id, String address, SyncMode syncMode) {
		return this.addManagedDatapoint(DatapointConfig.newConfig(id, address, syncMode));
	}

//	/**
//	 * @param id
//	 * @param address
//	 * @param agentId
//	 * @param syncMode
//	 * @return
//	 */
//	public CellFunctionConfig addManagedDatapoint(String id, String address, SyncMode syncMode) {
//		//Datapoint dp = (new DPBuilder()).newDatapoint(address);
//		return this.addManagedDatapoint(DatapointConfig.newConfig(id, address, syncMode));
//	}

	/**
	 * @return
	 */
	public List<DatapointConfig> getManagedDatapoints() {
		return this.getDatapointConfig(CELLMANAGEDDATAPOINTS);
	}

	/**
	 * @return
	 */
	public Map<String, DatapointConfig> getManagedDatapointsAsMap() {
		Map<String, DatapointConfig> result = new HashMap<>();

		this.getManagedDatapoints().forEach(s -> {
			result.put(s.getId(), s);
		});

		return result;
	}

	// =======================//

	/**
	 * @return
	 */
	public JsonObject toJsonObject() {
		return this.configObject;
	}

	// === Syncdatapoints ===//

	// =======================//

	// === Write datapoints ===//

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CellFunctionConfig [configObject=");
		builder.append(configObject);
		builder.append("]");
		return builder.toString();
	}

}
