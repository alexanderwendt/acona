package at.tuwien.ict.acona.mq.cell.config;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.core.CellImpl;

public class CellConfig {
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

	private final JsonObject configObject;

	/**
	 * Create cell config from name and class name
	 * 
	 * @param name
	 * @param className
	 * @return
	 */
	public static CellConfig newConfig(String name, String className) {
		return new CellConfig(name, className);
	}

	/**
	 * Create config from a cell name and a cell class
	 * 
	 * @param name
	 * @param clzz
	 * @return
	 */
	public static CellConfig newConfig(String name, Class<?> clzz) {
		return new CellConfig(name, clzz.getName());
	}

	/**
	 * Create config from a cell name. The cell class is the default CellImpl
	 * 
	 * @param name
	 * @return
	 */
	public static CellConfig newConfig(String name) {
		return new CellConfig(name, CellImpl.class.getName());
	}

	public static CellConfig newConfig(JsonObject config) throws Exception {
		return new CellConfig(config);
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
	private CellConfig(String name, String className) {
		this.configObject = new JsonObject();
		this.setName(name).setClassName(className);
		this.configObject.add(CELLFUNCTIONS, new JsonArray());

		// Add basic cell functions. Then nothing has to be generated in the code
		// this.addBasicCellFunctions();
	}

//	private void addBasicCellFunctions() {
//		this.addCellfunction(CellFunctionConfig.newConfig("write", BasicServiceWrite.class));
//		this.addCellfunction(CellFunctionConfig.newConfig("read", BasicServiceRead.class));
//		this.addCellfunction(CellFunctionConfig.newConfig("subscribe", BasicServiceSubscribe.class));
//		this.addCellfunction(CellFunctionConfig.newConfig("unsubscribe", BasicServiceUnsubscribe.class));
//		this.addCellfunction(CellFunctionConfig.newConfig("notify", BasicServiceNotifySubscribers.class));
//		this.addCellfunction(CellFunctionConfig.newConfig("remove", BasicServiceRemove.class));
//	}

	private CellConfig(JsonObject config) throws Exception {
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
	public CellConfig setName(String name) {
		this.configObject.addProperty(CELLNAME, name);
		return this;
	}

	/**
	 * Set MQTT host
	 * 
	 * @param name
	 * @return
	 */
	public CellConfig setHost(String name) {
		this.configObject.addProperty(HOST, name);
		return this;
	}

	/**
	 * Set MQTT username
	 * 
	 * @param name
	 * @return
	 */
	public CellConfig setUsername(String name) {
		this.configObject.addProperty(USERNAME, name);
		return this;
	}

	/**
	 * Set MQTT username
	 * 
	 * @param name
	 * @return
	 */
	public CellConfig setPassword(String name) {
		this.configObject.addProperty(PASSWORD, name);
		return this;
	}

	/**
	 * Set class name
	 * 
	 * @param className
	 * @return
	 */
	private CellConfig setClassName(String className) {
		this.configObject.addProperty(CELLCLASS, className);
		return this;
	}

	/**
	 * Set cell class
	 * 
	 * @param clzz
	 * @return
	 */
	public CellConfig setClass(Class<?> clzz) {
		this.setClassName(clzz.getName());
		return this;
	}

	public CellConfig setDescription(String description) {
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
	public CellConfig addProperty(String name, String value) {
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
	public CellConfig addProperty(String name, JsonObject value) {
		this.configObject.add(name, value);
		return this;
	}

	/**
	 * Add a cellfunction from cellfunction config
	 * 
	 * @param config
	 * @return
	 */
	public CellConfig addFunction(CellFunctionConfig config) {
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
	public CellConfig addFunction(String name, Class<?> clzz, Map<String, Object> params) {
		CellFunctionConfig config = CellFunctionConfig.newConfig(name, clzz, params);
		
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
	public CellConfig addFunction(String name, Class<?> clzz) {
		CellFunctionConfig config = CellFunctionConfig.newConfig(name, clzz, new HashMap<String, Object>());
		
		this.configObject.getAsJsonArray(CELLFUNCTIONS).add(config.toJsonObject());
		return this;
	}

	/**
	 * @param functionName
	 */
	public void removeCellFunctionConfig(String functionName) {
		JsonArray arr = this.configObject.getAsJsonArray(CELLFUNCTIONS).getAsJsonArray();
		int index = 0;
		for (int i = 0; i < arr.size(); i++) {
			JsonObject function = arr.get(i).getAsJsonObject();
			CellFunctionConfig config = CellFunctionConfig.newConfig(function);
			if (config.getName().equals(functionName)) {
				index = i;
				break;
			}
		}

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
	public CellConfig addProperty(String key, Object value) {
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
