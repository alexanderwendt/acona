package at.tuwien.ict.acona.cell.config;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.launcher.SystemConfigExternal;

public class SystemConfig implements SystemConfigExternal {
	public static final String TOPCONTROLLERNAME = "topcontrollername";
	public static final String CONTROLLERS = "controllers";
	public static final String SERVICES = "services";
	public static final String MEMORIES = "memories";

	private final JsonObject configObject;

	/**
	 * Create cell config from name and class name
	 * 
	 * @param name
	 * @param className
	 * @return
	 */
	public static SystemConfig newConfig() {
		return new SystemConfig();
	}

	public static SystemConfig newConfig(JsonObject config) throws Exception {
		return new SystemConfig(config);
	}

	private SystemConfig() {
		this.configObject = new JsonObject();
		this.configObject.add(TOPCONTROLLERNAME, JsonNull.INSTANCE);
		this.configObject.add(CONTROLLERS, new JsonArray());
		this.configObject.add(SERVICES, new JsonArray());
		this.configObject.add(MEMORIES, new JsonArray());
	}

	private SystemConfig(JsonObject config) throws Exception {
		super();
		if (this.isSystemConfig(config)) {
			this.configObject = config;
		} else {
			throw new Exception("This is no system config " + config);
		}

		// if (this.isSubscriptionConfig(config)==true) {
		// this.configObject = config;
		//
		// if (this.configObject.get(AGENTID)==null) {
		// this.setAgentId(LOCALAGENTNAME);
		// }
		// } else {
		// throw new Exception("The json is no subscription config");
		// }

	}

	public boolean isSystemConfig(JsonObject config) {
		boolean result = false;
		if (config.has(CONTROLLERS) && config.has(MEMORIES) && config.has(SERVICES)) {
			result = true;
		}

		return result;
	}

	@Override
	public SystemConfig setTopController(String name) {
		this.configObject.addProperty(TOPCONTROLLERNAME, name);
		return this;

	}

	@Override
	public String getTopController() {
		return this.configObject.getAsJsonPrimitive(TOPCONTROLLERNAME).getAsString();
	}

	@Override
	public SystemConfig addController(CellConfig controller) {
		this.configObject.getAsJsonArray(CONTROLLERS).add(controller.toJsonObject());
		return this;

	}

	@Override
	public SystemConfig addService(CellConfig controller) {
		this.configObject.getAsJsonArray(SERVICES).add(controller.toJsonObject());
		return this;

	}

	@Override
	public SystemConfig addMemory(CellConfig controller) {
		this.configObject.getAsJsonArray(MEMORIES).add(controller.toJsonObject());
		return this;
	}

	private List<CellConfig> getArrayData(String arrayName) {
		JsonArray array = this.configObject.getAsJsonArray(arrayName);
		// Gson gson = new Gson();
		// Type type = new TypeToken<List<SubscriptionConfig>>(){}.getType();
		// List<SubscriptionConfig> configList = gson.fromJson(array, type);
		List<CellConfig> result = new ArrayList<CellConfig>();
		array.forEach(a -> {

			try {
				result.add(CellConfig.newConfig(a.getAsJsonObject()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		return result;
	}

	public List<CellConfig> getControllers() {
		return this.getArrayData(CONTROLLERS);
	}

	public List<CellConfig> getServices() {
		return this.getArrayData(SERVICES);
	}

	public List<CellConfig> getMemories() {
		return this.getArrayData(MEMORIES);
	}
}
