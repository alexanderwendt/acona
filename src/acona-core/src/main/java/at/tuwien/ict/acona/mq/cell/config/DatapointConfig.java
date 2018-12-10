package at.tuwien.ict.acona.mq.cell.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

public class DatapointConfig {
	private final String id;
	private final String address;
	private final SyncMode syncMode;
	
	transient private Gson gson = new Gson();
	
	
	
	//public final static String LOCALAGENTNAME = "";
	// public final static SyncMode DEFAULTSYNCMODE = SyncMode.READONLY;

	//public static final String ID = "id";
	//public static final String ADDRESS = "address";	//Address must always be complete with agent name
	//public static final String AGENTID = "agentid";
	//public static final String SYNCMODE = "syncmode"; // pull, push, "" oder
														// null

	// Keep the jsonobject in order to be able to add more settings. If only a
	// class is used, flexibility is lost for creating
	// new json

	//private final JsonObject configObject;

	// public static DatapointConfig newConfig(String name, String address) {
	// return new DatapointConfig(name, address, LOCALAGENTNAME, DEFAULTSYNCMODE);
	// }

//	public static DatapointConfig newConfig(String name, String address, SyncMode syncmode) {
//		//Datapoint dp = (new DPBuilder()).newDatapoint(address);
//		super();
//		this.configObject = new JsonObject();
//		this.setId(id);
//		
//		return new DatapointConfig(name, address, syncmode);
//	}

	public static synchronized DatapointConfig newConfig(String name, String address, SyncMode syncmode) {
		Datapoint dp = (new DPBuilder()).newDatapoint(address);
		return new DatapointConfig(name, dp.getCompleteAddressAsTopic(""), syncmode);
	}

	public static DatapointConfig newConfig(JsonObject config) throws Exception {
		return new DatapointConfig(config);
	}

	private DatapointConfig(String id, String address, SyncMode syncMode) {
		this.id = id;
		this.address = address;
		this.syncMode = syncMode;
		
		
//		//super();
//		//this.configObject = new JsonObject();
//		this.setId(id);
//		//Datapoint dp = (new DPBuilder()).newDatapoint(address);
//		this.setAddress(address);
//		//this.setAgentId(agentid);
//		this.setSyncMode(syncmode);
	}

	private DatapointConfig(JsonObject config) throws Exception {
		//super();
		if (this.isSubscriptionConfig(config) == true) {
			
			DatapointConfig obj = gson.fromJson(config, DatapointConfig.class);
			this.address = obj.address;
			this.id = obj.id;
			this.syncMode = obj.syncMode;
			//this.configObject = config;
			

//			if (this.configObject.get(AGENTID) == null) {
//				this.setAgentId(LOCALAGENTNAME);
//			}
		} else {
			throw new Exception("The json is no subscription config");
		}

	}

//	private void setId(String id) {
//		this.configObject.addProperty(ID, id);
//	}

//	private void setAddress(String id) {
//		this.configObject.addProperty(ADDRESS, id);
//	}

//	private void setAgentId(String id) {
//		this.configObject.addProperty(AGENTID, id);
//	}

//	private void setSyncMode(SyncMode mode) {
//		this.configObject.addProperty(SYNCMODE, mode.toString());
//	}

	public String getId() {
		return this.id;
		//return this.configObject.get(ID).getAsString();
	}

	public String getAddress() {
		return this.address;
		//return this.configObject.get(ADDRESS).getAsString();
	}

	/**
	 * Return the agent id of the address
	 * 
	 * @param callerAgentName
	 * @return
	 */
	public String getAgentid() {
		
		Datapoint dp = (new DPBuilder()).newDatapoint(this.getAddress());
		
//		String agentName = this.configObject.get(AGENTID).getAsString();
//
//		if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
//			agentName = callerAgentName;
//		}

		return dp.getAgent();
	}

	// private boolean hasAgentId() {
	// String agentName = this.configObject.get(AGENTID).getAsString();
	//
	// boolean result = true;
	//
	// if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
	// result = false;
	// }
	//
	// return result;
	// }

	public SyncMode getSyncMode() {
		return this.syncMode;
		//return SyncMode.valueOf(this.configObject.get(SYNCMODE).getAsString());
	}

	public JsonObject toJsonObject() {
		return gson.toJsonTree(this).getAsJsonObject();
	}

	public boolean isSubscriptionConfig(JsonObject testObject) {
		boolean result = false;
		if (testObject.get("address") != null && testObject.get("address").isJsonPrimitive() == true
				&& testObject.get("id") != null && testObject.get("id").isJsonPrimitive() == true) {
			result = true;
		}

		return result;
	}

//	public String getComposedAddress(String defaultAgentName) {
//		String destinationAgent = this.getAgentid(defaultAgentName);
//		String address = this.getAddress();
//
//		// Generate key for the internal activator
//		String key = destinationAgent + ":" + address;
//
//		return key;
//	}

	public Datapoint toDatapoint() {
		return (new DPBuilder()).newDatapoint(this.getAddress());
	}
	
	@Override
	public String toString() {
		return id + "|" + address + "|" + syncMode;
	}

}
