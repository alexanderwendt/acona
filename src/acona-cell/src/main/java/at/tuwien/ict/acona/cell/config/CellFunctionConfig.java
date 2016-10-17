package at.tuwien.ict.acona.cell.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.SyncMode;

public class CellFunctionConfig {
	public static final String CELLFUNCTIONNAME = "functionname";
	public static final String CELLFUNCTIONCLASS = "functionclass";
	public static final String CELLSYNCDATAPOINTS = "syncdatapoints";
	public static final String CELLWRITEDATAPOINTS = "writedatapoints";
	public static final String CELLEXECUTERATE = "executerate";
	public static final String CELLEXECUTEONCE = "executeonce";

	private static Logger log = LoggerFactory.getLogger(CellFunctionConfig.class);

	protected final JsonObject configObject;

	public static CellFunctionConfig newConfig(String name, String className) {
		return new CellFunctionConfig(name, className);
	}

	public static CellFunctionConfig newConfig(String name, Class<?> clzz) {
		String className = clzz.getName();
		return new CellFunctionConfig(name, className);
	}

	/**
	 * Config, where the function name is the class name for a simple class +
	 * hashcode
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
		this.configObject = new JsonObject();
		this.configObject.add(CELLSYNCDATAPOINTS, new JsonArray());
		this.configObject.add(CELLWRITEDATAPOINTS, new JsonArray());
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

	//=== Syncdatapoints ===//

	public CellFunctionConfig addSyncDatapoint(DatapointConfig config) {
		this.configObject.getAsJsonArray(CELLSYNCDATAPOINTS).add(config.toJsonObject());
		return this;
	}

	public CellFunctionConfig addSyncDatapoint(String address) {
		return this.addSyncDatapoint(DatapointConfig.newConfig(address, address));
	}

	public CellFunctionConfig addSyncDatapoint(String id, String address, String agentId, SyncMode syncMode) {
		return this.addSyncDatapoint(DatapointConfig.newConfig(id, address, agentId, syncMode));
	}

	public List<DatapointConfig> getSyncDatapoints() {
		return this.getDatapointConfig(CELLSYNCDATAPOINTS);
	}

	public Map<String, DatapointConfig> getSyncDatapointsAsMap() {
		Map<String, DatapointConfig> result = new HashMap<String, DatapointConfig>();

		this.getSyncDatapoints().forEach(s -> {
			result.put(s.getId(), s);
		});

		return result;
	}

	//=======================//

	//=== Write datapoints ===//

	public CellFunctionConfig addWriteDatapoint(DatapointConfig config) {
		this.configObject.getAsJsonArray(CELLWRITEDATAPOINTS).add(config.toJsonObject());
		return this;
	}

	public CellFunctionConfig addWriteDatapoint(String address) {
		return this.addWriteDatapoint(DatapointConfig.newConfig(address, address));
	}

	public CellFunctionConfig addWriteDatapoint(String id, String address, String agentId, SyncMode syncMode) {
		return this.addWriteDatapoint(DatapointConfig.newConfig(id, address, agentId, syncMode));
	}

	public Map<String, DatapointConfig> getWriteDatapointsAsMap() {
		Map<String, DatapointConfig> result = new HashMap<String, DatapointConfig>();

		this.getWriteDatapoints().forEach(s -> {
			result.put(s.getId(), s);
		});

		return result;
	}

	public List<DatapointConfig> getWriteDatapoints() {
		return this.getDatapointConfig(CELLWRITEDATAPOINTS);
	}

	//======================//

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

	private List<DatapointConfig> getDatapointConfig(String type) {
		JsonArray array = this.configObject.getAsJsonArray(type);
		// Gson gson = new Gson();
		// Type type = new TypeToken<List<SubscriptionConfig>>(){}.getType();
		// List<SubscriptionConfig> configList = gson.fromJson(array, type);
		List<DatapointConfig> result = new ArrayList<DatapointConfig>();
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

	public String getProperty(String key) throws Exception {
		String result = "";
		try {
			result = this.configObject.getAsJsonPrimitive(key).getAsString();
		} catch (Exception e) {
			throw new Exception("Cannot read key " + key + ", " + e);
		}

		return result;
	}

	public String getProperty(String key, String defaultValue) {
		String result = defaultValue;

		if (configObject.has(key)) {
			result = this.configObject.getAsJsonPrimitive(key).getAsString();
		}

		return result;
	}

	public CellFunctionConfig setProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}

	public CellFunctionConfig setProperty(String name, JsonObject value) {
		this.configObject.add(name, value);
		return this;
	}

	public JsonObject getPropertyAsJsonObject(String key) {
		return this.configObject.getAsJsonObject(key);
	}

	public JsonObject toJsonObject() {
		return this.configObject;
	}

	public <DATA_TYPE> DATA_TYPE getProperty(String key, Class<DATA_TYPE> type) {
		Gson gson = new Gson();

		return gson.fromJson(this.configObject.get(key), type);
	}

	public void setProperty(String key, Object value) {
		// TODO: Method not tested yet
		this.configObject.add(key, new Gson().toJsonTree(value));
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
