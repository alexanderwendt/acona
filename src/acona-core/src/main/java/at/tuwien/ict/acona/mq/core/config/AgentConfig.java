package at.tuwien.ict.acona.mq.core.config;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.core.core.AgentImpl;

public class AgentConfig {
	public static final String HOST = "host";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String CELLNAME = "cellname";
	public static final String CELLCLASS = "cellclass";
	public static final String CELLDESCRIPTION = "celldescription";
	public static final String CELLFUNCTIONS = "cellfunctions";

	// Keep the jsonobject in order to be able to add more settings. If only a
	// class is used, flexibility is lost for creating
	// new json
	
	private static final String defaulthost = "tcp://127.0.0.1:1883";
	private static final String defaultusername = "acona";
	private static final String defaultpassword = "acona";

	private final JsonObject configObject;

	/**
	 * Create cell config from name and class name
	 * 
	 * @param name
	 * @param className
	 * @return
	 */
	public static AgentConfig newConfig(String name, String className) {
		return new AgentConfig(name, className).setHost(defaulthost).setUsername(defaultusername).setPassword(defaultpassword);
	}

	/**
	 * Create config from a cell name and a cell class
	 * 
	 * @param name
	 * @param clzz
	 * @return
	 */
	public static AgentConfig newConfig(String name, Class<?> clzz) {
		return new AgentConfig(name, clzz.getName()).setHost(defaulthost).setUsername(defaultusername).setPassword(defaultpassword);
	}

	/**
	 * Create config from a cell name. The cell class is the default CellImpl
	 * 
	 * @param name
	 * @return
	 */
	public static AgentConfig newConfig(String name) {
		return new AgentConfig(name, AgentImpl.class.getName()).setHost(defaulthost).setUsername(defaultusername).setPassword(defaultpassword);
	}

	public static AgentConfig newConfig(JsonObject config) throws Exception {
		return new AgentConfig(config);
	}

	public boolean isCellConfig(JsonObject config) {
		boolean result = false;
		if (config.has(CELLCLASS) && config.has(CELLFUNCTIONS) && config.has(CELLNAME)) {
			result = true;
		}

		return result;
	}

	/**
	 * private constructor to create a config from cell name and cell class
	 * 
	 * @param name
	 * @param className
	 */
	private AgentConfig(String name, String className) {
		this.configObject = new JsonObject();
		this.setName(name).setClassName(className);
		this.configObject.add(CELLFUNCTIONS, new JsonArray());

		// Add basic cell functions. Then nothing has to be generated in the code
		// this.addBasicCellFunctions();
	}

	private AgentConfig(JsonObject config) throws Exception {
		if (this.isCellConfig(config)) {
			this.configObject = config;
		} else {
			throw new Exception("This is no cellconfig: " + config);
		}
	}

	/**
	 * Set name of cell
	 * 
	 * @param name
	 * @return
	 */
	public AgentConfig setName(String name) {
		this.configObject.addProperty(CELLNAME, name);
		return this;
	}

	/**
	 * Set MQTT host
	 * 
	 * @param name
	 * @return
	 */
	public AgentConfig setHost(String name) {
		this.configObject.addProperty(HOST, name);
		return this;
	}

	/**
	 * Set MQTT username
	 * 
	 * @param name
	 * @return
	 */
	public AgentConfig setUsername(String name) {
		this.configObject.addProperty(USERNAME, name);
		return this;
	}

	/**
	 * Set MQTT username
	 * 
	 * @param name
	 * @return
	 */
	public AgentConfig setPassword(String name) {
		this.configObject.addProperty(PASSWORD, name);
		return this;
	}

	/**
	 * Set class name
	 * 
	 * @param className
	 * @return
	 */
	private AgentConfig setClassName(String className) {
		this.configObject.addProperty(CELLCLASS, className);
		return this;
	}

	/**
	 * Set cell class
	 * 
	 * @param clzz
	 * @return
	 */
	public AgentConfig setClass(Class<?> clzz) {
		this.setClassName(clzz.getName());
		return this;
	}

	public AgentConfig setDescription(String description) {
		this.configObject.addProperty(CELLDESCRIPTION, description);
		return this;
	}

	/**
	 * Get cell class as a class
	 * 
	 * @return
	 * @throws Exception
	 */
	public Class<?> getClassToInvoke() throws Exception {
		return Class.forName(this.getClassName());
	}

	/**
	 * Add custom property for a string value
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public AgentConfig addProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}

	/**
	 * Add custom property for a Json-Value
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public AgentConfig addProperty(String name, JsonObject value) {
		this.configObject.add(name, value);
		return this;
	}

	/**
	 * Add a cellfunction from cellfunction config
	 * 
	 * @param config
	 * @return
	 */
	public AgentConfig addFunction(FunctionConfig config) {
		this.configObject.getAsJsonArray(CELLFUNCTIONS).add(config.toJsonObject());
		return this;
	}
	
	/**
	 * Add a cell function from function values with parameters. 
	 * 
	 * @param name
	 * @param clzz
	 * @param params
	 * @return
	 */
	public AgentConfig addFunction(String name, Class<?> clzz, Map<String, Object> params) {
		FunctionConfig config = FunctionConfig.newConfig(name, clzz, params);
		if (params.containsKey(FunctionConfig.COMMUNICATORTIMEOUT)==true) {
			config.setCommunicatorTimeout(Integer.valueOf(params.get(FunctionConfig.COMMUNICATORTIMEOUT).toString()));
		}
		this.configObject.getAsJsonArray(CELLFUNCTIONS).add(config.toJsonObject());
		return this;
	}
	
	/**
	 * Add a cell function from function values without any custom parameters. 
	 * 
	 * @param name
	 * @param clzz
	 * @return
	 */
	public AgentConfig addFunction(String name, Class<?> clzz) {
		FunctionConfig config = FunctionConfig.newConfig(name, clzz, new HashMap<String, Object>());
		
		this.configObject.getAsJsonArray(CELLFUNCTIONS).add(config.toJsonObject());
		return this;
	}
	
	/**
	 * Get a cell function by name
	 * 
	 * @param name
	 * @return
	 */
	public FunctionConfig getCellFunction(String name) {
		FunctionConfig result = null;
		
		for (JsonElement e : this.getCellfunctions()) {
			if (e.getAsJsonObject().get(FunctionConfig.CELLFUNCTIONNAME).getAsString().equals(name)) {
				result = FunctionConfig.newConfig(e.getAsJsonObject());
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Replaces or adds an existing cellfunction config or adds it new
	 * 
	 * @param config
	 */
	public void replaceCellFunctionConfig(FunctionConfig config) {
		this.removeCellFunctionConfig(config.getName());
		
		this.addFunction(config);
	}
	
	/**
	 * Get the position of a cell function in the function array
	 * 
	 * @param name
	 * @return int [0,inf] for position and -1 if not found.
	 */
	private int getCellFunctionConfigIndex(String name) {
		JsonArray arr = this.configObject.getAsJsonArray(CELLFUNCTIONS).getAsJsonArray();
		int result = -1;
		
		for (int i = 0; i < arr.size(); i++) {
			JsonObject function = arr.get(i).getAsJsonObject();
			FunctionConfig config = FunctionConfig.newConfig(function);
			if (config.getName().equals(name)) {
				result = i;
				break;
			}
		}
		
		return result;
	}

	/**
	 * @param functionName
	 */
	public void removeCellFunctionConfig(String functionName) {		
		int index = this.getCellFunctionConfigIndex(functionName);

		this.configObject.getAsJsonArray(CELLFUNCTIONS).remove(index);
	}

	/**
	 * Get all cellfunctions
	 * 
	 * @return
	 */
	public JsonArray getCellfunctions() {
		return this.configObject.getAsJsonArray(CELLFUNCTIONS);
	}

	/**
	 * Get cell name
	 * 
	 * @return
	 */
	public String getName() {
		return this.configObject.getAsJsonPrimitive(CELLNAME).getAsString();
	}

	/**
	 * Get host
	 * 
	 * @return
	 */
	public String getHost() {
		return this.configObject.getAsJsonPrimitive(HOST).getAsString();
	}

	/**
	 * Get username
	 * 
	 * @return
	 */
	public String getUsername() {
		return this.configObject.getAsJsonPrimitive(USERNAME).getAsString();
	}

	/**
	 * Get password
	 * 
	 * @return
	 */
	public String getPassword() {
		return this.configObject.getAsJsonPrimitive(PASSWORD).getAsString();
	}

	/**
	 * Get class name
	 * 
	 * @return
	 */
	public String getClassName() {
		return this.configObject.getAsJsonPrimitive(CELLCLASS).getAsString();
	}

	/**
	 * Get the decription of the cell
	 * 
	 * @return
	 */
	public String getDescription() {
		return this.configObject.getAsJsonPrimitive(CELLDESCRIPTION).getAsString();
	}

	/**
	 * Get a custom proerty
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return this.configObject.getAsJsonPrimitive(key).getAsString();
	}

	/**
	 * Get a custom property and transform it into a target data structure
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public <DATA_TYPE> DATA_TYPE getProperty(String key, Class<DATA_TYPE> type) {
		Gson gson = new Gson();

		return gson.fromJson(this.configObject.get(key), type);
	}

	/**
	 * Add a custom property from any object that can be serialized by Json
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public AgentConfig addProperty(String key, Object value) {
		// TODO: Method not tested yet
		this.configObject.add(key, new Gson().toJsonTree(value));
		return this;
	}

	/**
	 * Get cell config as Json object
	 * 
	 * @return
	 */
	public JsonObject toJsonObject() {
		return this.configObject;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(configObject);
		return builder.toString();
	}
}
