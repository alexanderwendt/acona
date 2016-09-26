package at.tuwien.ict.acona.cell.config;

import com.google.gson.JsonObject;

public class SubscriptionConfig {
	public final static String LOCALAGENTNAME = "";
	
	public static final String ID = "id";
	public static final String ADDRESS = "address";
	public static final String AGENTID = "agentid";
	
	//Keep the jsonobject in order to be able to add more settings. If only a class is used, flexibility is lost for creating
	//new json
	
	private final JsonObject configObject;	
	
	public static SubscriptionConfig newConfig(String name, String address) {
		return new SubscriptionConfig(name, address);
	}
	
	public static SubscriptionConfig newConfig(String name, String address, String agentid) {
		return new SubscriptionConfig(name, address, agentid);
	}
	
	public static SubscriptionConfig newConfig(JsonObject config) throws Exception {
		return new SubscriptionConfig(config);
	}
	
	private SubscriptionConfig(String id, String address, String agentid) {
		super();
		this.configObject = new JsonObject();
		this.setId(id);
		this.setAddress(address);
		this.setAgentId(agentid);
	}
	
	private SubscriptionConfig(String id, String address) {
		super();
		this.configObject = new JsonObject();
		this.setId(id);
		this.setAddress(address);
		this.setAgentId(LOCALAGENTNAME);
	}
	
	private SubscriptionConfig(JsonObject config) throws Exception {
		super();
		if (this.isSubscriptionConfig(config)==true) {
			this.configObject = config;
			
			if (this.configObject.get(AGENTID)==null) {
				this.setAgentId(LOCALAGENTNAME);
			}
		} else {
			throw new Exception("The json is no subscription config");
		}
		
	}
	
	private void setId(String id) {
		this.configObject.addProperty(ID, id);
	}
	
	private void setAddress(String id) {
		this.configObject.addProperty(ADDRESS, id);
	}
	
	private void setAgentId(String id) {
		this.configObject.addProperty(AGENTID, id);
	}

	public String getId() {
		return this.configObject.get(ID).getAsString();
	}

	public String getAddress() {
		return this.configObject.get(ADDRESS).getAsString();
	}

	public String getAgentid() {
		return this.configObject.get(AGENTID).getAsString();
	}
	
	public JsonObject toJsonObject() {
		return this.configObject;
	}
	
	public boolean isSubscriptionConfig(JsonObject testObject) {
		boolean result = false;
		if (testObject.get(ADDRESS)!=null && testObject.get(ADDRESS).isJsonPrimitive()==true && testObject.get(ID)!=null && testObject.get(ID).isJsonPrimitive()==true) {
			result = true;
		}
		
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriptionConfig [configObject=");
		builder.append(configObject);
		builder.append("]");
		return builder.toString();
	}

	
}
