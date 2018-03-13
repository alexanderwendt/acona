package at.tuwien.ict.acona.cell.cellfunction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import jade.domain.FIPANames;

public abstract class CellFunctionImpl implements CellFunction {

	private static final Logger log = LoggerFactory.getLogger(CellFunctionImpl.class);

	/**
	 * Cell, which executes this function
	 */
	private Cell cell;
	private CellFunctionConfig config;

	/**
	 * Name of the activator
	 */
	private String cellFunctionName;

	/**
	 * List of datapoints that shall be subscribed
	 */
	private final Map<String, DatapointConfig> subscriptionConfigs = new ConcurrentHashMap<>(); // Variable datapoint config id, datapoint
	private final Map<String, DatapointConfig> readDatapointConfigs = new ConcurrentHashMap<>(); // Variable, datapoint
	private final Map<String, DatapointConfig> writeDatapointConfigs = new ConcurrentHashMap<>();
	private final Map<String, DatapointConfig> managedDatapointConfigs = new ConcurrentHashMap<>();

	/**
	 * Current state of the service. Every time the service is set, a datapoint is updated.
	 */
	private ServiceState currentServiceState = ServiceState.INITIALIZING;

	protected final MonitoringObject monitoringObject = new MonitoringObject();

	/**
	 * Constructor
	 */
	public CellFunctionImpl() {

	}

	@Override
	public void init(CellFunctionConfig config, Cell caller) throws Exception {
		try {
			// === Extract fundamental settings ===//
			// Extract settings
			this.config = config;
			this.cell = caller;

			// Get the settings but set also default values

			// Get name
			this.cellFunctionName = this.config.getName();

			log.trace("Initialize an agent with config:{}", config);

			// === Internal init ===//
			this.setServiceState(ServiceState.INITIALIZING);

			// Possibility to add more subscriptions and to overwrite default
			// settings
			cellFunctionInit();

			// === Extract execution settings ===//

			// Get responder if it exists
			if (this.getFunctionConfig().getGenerateReponder() == null) {
				this.getFunctionConfig().setGenerateReponder(false);
			}

			if (this.getFunctionConfig().getResponderProtocol().equals("") == true) {
				// Set default responderprotocol
				this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			}

			// if (this.getFunctionConfig().getRegisterState() == null) {
			// this.getFunctionConfig().setRegisterState(false);
			// }

			// Get subscriptions from config and add to subscription list
			this.getFunctionConfig().getManagedDatapoints().forEach(s -> {
				this.addManagedDatapoint(s);
			});

			// === Register in the function handler ===//
			this.getCell().getFunctionHandler().registerCellFunctionInstance(this); // Here are also the default subscriptions

			// === Register subscriptions === //
			this.subscribeDatapoints();

		} catch (Exception e) {
			log.error("Cannot init function with config={}", config, e);
			// this.shutDown();
			throw new Exception(e.getMessage());
		}

		this.setServiceState(ServiceState.FINISHED);

		log.debug("Function={} initialized. Sync datapoints={}", this.getFunctionName(), this.managedDatapointConfigs);
	}

	protected abstract void cellFunctionInit() throws Exception;

	/**
	 * Add a datapoint that shall be syncronized with read, subscribe or write. this method should be used in the init method of a function
	 * 
	 * @param config
	 */
	protected void addManagedDatapoint(DatapointConfig config) {
		if (config.getSyncMode().equals(SyncMode.SUBSCRIBEONLY)) { // Subscribe only
			this.subscriptionConfigs.put(config.getId(), config);
		} else if (config.getSyncMode().equals(SyncMode.SUBSCRIBEWRITEBACK)) { // Subscribe and write back to the source
			this.subscriptionConfigs.put(config.getId(), config);
			this.writeDatapointConfigs.put(config.getId(), config);
		} else if (config.getSyncMode().equals(SyncMode.READONLY)) { // Read only the value (pull instead of push)
			this.readDatapointConfigs.put(config.getId(), config);
		} else if (config.getSyncMode().equals(SyncMode.READWRITEBACK)) { // Read and write back to the server
			this.readDatapointConfigs.put(config.getId(), config);
			this.writeDatapointConfigs.put(config.getId(), config);
		} else if (config.getSyncMode().equals(SyncMode.WRITEONLY)) {
			this.writeDatapointConfigs.put(config.getId(), config);
		} else {
			try {
				throw new Exception("No syncmode=" + config.getSyncMode() + ". only pull and push available");
			} catch (Exception e) {
				log.error("Cannot set sync mode", e);
			}
		}

		// All datapoints are put into the managed datapoints
		managedDatapointConfigs.put(config.getId(), config);
	}

	private void subscribeDatapoints() throws Exception {
		// List<DatapointConfig> activatorAddresses = new ArrayList<>(this.getSubscribedDatapoints().values());

		// Go through each address and add the activator to this address
		// for (DatapointConfig subscriptionConfig : activatorAddresses) {
		this.getSubscribedDatapoints().values().forEach(subscriptionConfig -> {
			try {
				// Adds the subscription to the handler
				// Subscribe the datapoint
				// Construct name (if only "", then use the local agent)
				// String agentName = subscriptionConfig.getAgentid(this.hostCell.getLocalName());
				// String key = agentName + ":" + subscriptionConfig.getAddress();

				// Add subscription to the subscription handler
				// Add it first. On subscription, the deafault value can be sent to the update function.
				// this.getCell().getSubscriptionHandler().addSubscription(this.getFunctionName(), subscriptionConfig.getComposedAddress(this.getAgentName()));

				// String key = this.generateKey(subscriptionConfig);
				// Datapoint dp = Datapoints.newDatapoint(key);
				// if (this.datapointActivationMap.containsKey(key) == false) {
				String completeAddress = subscriptionConfig.getComposedAddress(this.getAgentName());
				@SuppressWarnings("unused")
				Datapoint initialValue = this.getCommunicator().subscribeDatapoint(completeAddress, this.getFunctionName());
				log.debug("{}>Subscribed address={}.", this.getAgentName(), subscriptionConfig.getComposedAddress(completeAddress));
				// } else {
				// log.debug("Key={} already exists in the function mapping. Therefore no additional subscription is necessary", key);
				// }
			} catch (Exception e) {
				log.error("Cannot subscribe address={}", this.getAgentName() + ":" + subscriptionConfig.getAddress(), e);
				// throw new Exception(e.getMessage());
			}
		});
	}

	public void removeSubscription() {
		// Get all subscribed addresses
		// List<DatapointConfig> activatorAddresses = new ArrayList<>(this.getSubscribedDatapoints().values());

		// Go through each address and remove the activator to this address
		this.getSubscribedDatapoints().values().forEach(subscriptionsConfig -> {
			// activatorAddresses.forEach(subscriptionsConfig -> {
			try {
				this.getCommunicator().unsubscribeDatapoint(subscriptionsConfig.getComposedAddress(this.getAgentName()), this.getFunctionName());
				// this.getCell().getSubscriptionHandler().removeSubscription(this.getFunctionName(), subscriptionsConfig.getComposedAddress(this.getAgentName()));

				// //String key = subscriptionsConfig.getAgentid(this.hostCell.getLocalName()) + ":" + subscriptionsConfig.getAddress();
				// if (this.getCell().getSubscriptionHandler().removeSubscription(cellFunctionInstance, key);.datapointActivationMap.containsKey(key) == false) {
				// this.hostCell.getCommunicator().unsubscribe(subscriptionsConfig.getAgentid(this.hostCell.getLocalName()), Arrays.asList(subscriptionsConfig.getAddress()));
				// log.debug("Datapoint {}:{} was deregistered as no functions subscribes it", this.hostCell.getLocalName(), key, activatorInstance.getFunctionName());
				// } else {
				// log.debug("Datapoint {}:{} was deregistered for the function {}. It is still subscribed by the agent.", this.hostCell.getLocalName(), key, activatorInstance.getFunctionName());
				// }
				//
				// //this.removeSubscription(activatorInstance.getFunctionName(), subscriptionsConfig.getAgentid(), subscriptionsConfig.getAddress());
				// this.removeSubscription(this.getFunctionName(), subscriptionsConfig.getComposedAddress(this.getAgentName()));

			} catch (Exception e) {
				log.error("Cannot unsubscribe address={}", subscriptionsConfig.getAddress(), e);
			}

		});
	}

	// private String generateKey(DatapointConfig subscriptionConfig) {
	// String destinationAgent = subscriptionConfig.getAgentid(this.getCell().getLocalName());
	// String address = subscriptionConfig.getAddress();
	//
	// //Construct name (if only "", then use the local agent)
	// String agentName = destinationAgent;
	// if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
	// agentName = this.getCell().getLocalName();
	// }
	//
	// //Generate key for the internal activator
	// String key = agentName + ":" + address;
	//
	// return key;
	// }

	@Override
	public void shutDown() {
		// Unsubscribe all datapoints
		// this.getCell().getFunctionHandler().deregisterActivatorInstance(this);
		try {
			// Execute specific functions
			this.shutDownImplementation();

			// Remove all subscriptions
			this.removeSubscription();

			// Execute general deregister
			this.getCell().getFunctionHandler().deregisterActivatorInstance(this.getFunctionName());
			// this.getCell().takeDownCell();
			log.debug("Agent {}> ==== shut down function={} ====", this.getCell().getLocalName(), this.getFunctionName());
		} catch (Exception e) {
			log.error("No clean shutdown possible", e);
		}
	}

	protected abstract void shutDownImplementation() throws Exception;

	@Override
	public void updateSubscribedData(final Map<String, Datapoint> data, final String caller) {
		synchronized (this.monitoringObject) {
			// Create datapointmapping ID to datapoint with new value
			Map<String, Datapoint> subscriptions = new HashMap<>();
			this.getSubscribedDatapoints().forEach((k, v) -> {
				if (data.containsKey(v.getAddress())) {
					subscriptions.put(k, data.get(v.getAddress()));
				}
			});

			this.updateDatapointsById(subscriptions);
		}
	}

	protected abstract void updateDatapointsById(Map<String, Datapoint> data);

	@Override
	public String getFunctionName() {
		return this.cellFunctionName;
	}

	@Override
	public String getAgentName() {
		return this.getCell().getLocalName();
	}

	@Override
	public Map<String, DatapointConfig> getSubscribedDatapoints() { // ID config
		return subscriptionConfigs;
	}

	@Override
	public CellFunctionConfig getFunctionConfig() {
		return this.config;
	}

//	private void setFunctionConfig(JsonObject config) throws Exception {
//		// Set new config
//		this.config = CellFunctionConfig.newConfig(config);
//
//		// Restart system
//		this.shutDown();
//		this.init(this.config, this.cell);
//	}

	// === read and write shortcuts ===//

	protected Communicator getCommunicator() {
		return this.getCell().getCommunicator();
	}

	protected void writeLocal(Datapoint datapoint) throws Exception {
		this.getCommunicator().write(datapoint);
	}

	protected <DATATYPE> void writeLocal(String address, DATATYPE datapoint) throws Exception {
		Gson gson = new Gson();
		String value = gson.toJson(datapoint);
		this.getCommunicator().write(DatapointBuilder.newDatapoint(address).setValue(value));
	}

	protected Datapoint readLocal(String address) throws Exception {
		return this.getCommunicator().read(address);
	}

	protected JsonElement readLocalAsJson(String address) throws Exception {
		return this.getCommunicator().read(address).getValue();
	}

	protected <T> T readLocalById(String id, Class<T> type) throws Exception {
		Gson gson = new Gson();
		JsonElement value = this.readLocal(this.getFunctionConfig().getManagedDatapointsAsMap().get(id).getAddress())
				.getValue();
		T convertedValue = gson.fromJson(value, type);

		return convertedValue;
	}

	protected Datapoint readLocalId(String id) throws Exception {
		return this.readLocal(this.getSyncDatapointConfigs().get(id).getAddress());
	}

	protected <T> void writeLocalSyncDatapointById(String id, T value) throws Exception {
		// Gson gson = new Gson();

		JsonElement writeValue = new Gson().toJsonTree(value);
		this.writeLocal(DatapointBuilder.newDatapoint(this.getFunctionConfig().getManagedDatapointsAsMap().get(id).getAddress())
				.setValue(writeValue));
	}

	protected <T> T getCustomSetting(String key, Class<T> type) {
		return this.getFunctionConfig().getProperty(cellFunctionName, type);
	}

	protected Cell getCell() {
		return cell;
	}

	/**
	 * Return the subscribed datapoint based on its ID in the function
	 * 
	 * @param data:
	 *            inputmap from subscribed data
	 * @param id:
	 *            datapoint id defined in the config or in the code
	 * @return
	 */
	protected Datapoint getDatapointFromId(Map<String, Datapoint> data, String id) {
		return data.get(this.getSyncDatapointConfigs().get(id).getAddress());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CellFunctionImpl [name=");
		builder.append(cellFunctionName);
		builder.append(", subscriptions=");
		builder.append(subscriptionConfigs);
		builder.append("]");
		return builder.toString();
	}

	protected Map<String, DatapointConfig> getSyncDatapointConfigs() {
		return managedDatapointConfigs;
	}

	protected Map<String, DatapointConfig> getReadDatapointConfigs() {
		return readDatapointConfigs;
	}

	protected Map<String, DatapointConfig> getWriteDatapointConfigs() {
		return writeDatapointConfigs;
	}

	@Override
	public ServiceState getCurrentState() {
		return this.currentServiceState;

	}

	/**
	 * Set the current service state
	 * 
	 * @param serviceState
	 * @throws Exception
	 */
	protected void setServiceState(ServiceState serviceState) throws Exception {
		this.currentServiceState = serviceState;
		// if (this.getFunctionConfig().getRegisterState().getAsBoolean() == true) {
		// this.getCell().getFunctionHandler().updateState(this, this.currentServiceState);
		// }
		this.processServiceState();
	}

	protected void processServiceState() throws Exception {

	}

	@Override
	public CellFunctionType getFunctionType() {
		return CellFunctionType.BASEFUNCTION;
	}

}
