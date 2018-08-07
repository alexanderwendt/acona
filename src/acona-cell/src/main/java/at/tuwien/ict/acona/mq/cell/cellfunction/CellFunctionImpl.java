package at.tuwien.ict.acona.mq.cell.cellfunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicatorImpl;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public abstract class CellFunctionImpl implements CellFunction {

	private static final Logger log = LoggerFactory.getLogger(CellFunctionImpl.class);

	/**
	 * Cell, which executes this function
	 */
	private Cell cell;
	private CellFunctionConfig config;

	private MqttCommunicator comm;

	private final DPBuilder dpBuilder = new DPBuilder();

	// Service Classes for this specific function
	private final Map<String, Function<Request, Response>> handlerMap = new HashMap<>();

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

	// protected final MonitoringObject monitoringObject = new MonitoringObject();

	/**
	 * 
	 */
	private final Map<String, Function<Request, Response>> functions = new HashMap<>();

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

			this.comm = new MqttCommunicatorImpl(this.cell.getDataStorage());

			// Get the settings but set also default values

			// Get name
			this.cellFunctionName = this.config.getName();

			log.trace("Initialize an agent with config:{}", config);

			// === Internal init ===//
			this.setServiceState(ServiceState.INITIALIZING);

			// Possibility to add more subscriptions and to overwrite default
			// settings
			cellFunctionInit();

			functions.entrySet().forEach(e -> {
				try {
					this.addRequestHandlerFunction(e.getKey(), e.getValue());
					log.debug("Added request method={}", e.getKey());
				} catch (Exception e1) {
					log.error("Cannot add function {}", e);
				}
			});

			// === Extract execution settings ===//

			// Get responder if it exists
			// if (this.getFunctionConfig().getGenerateReponder() == null) {
			// this.getFunctionConfig().setGenerateReponder(false);
			// }

			// if (this.getFunctionConfig().getResponderProtocol().equals("") == true) {
			// // Set default responderprotocol
			// this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			// }

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
	 * Add a function that is triggered by incoming request
	 * 
	 * @param topicSuffix
	 * @param function
	 * @throws Exception
	 */
	protected void addRequestHandlerFunction(String topicSuffix, Function<Request, Response> function) throws Exception {
		this.handlerMap.put(this.getCellName() + "/" + topicSuffix, function);

		try {
			this.getCommunicator().subscribeTopic(this.getCellName() + "/" + topicSuffix);
		} catch (MqttException e) {
			log.error("Cannot subscribe input to service function {}", this.getCellName() + "/" + topicSuffix);
			throw new Exception(e.getMessage());
		}
		log.info("Added function to {}", this.getCellName() + "/" + topicSuffix);

	}

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
				String completeAddress = subscriptionConfig.getComposedAddress(this.getCellName());
				// @SuppressWarnings("unused")
				Datapoint initialValue = this.getCommunicator().subscribeDatapoint(completeAddress);
				log.debug("{}>Subscribed address={}.", this.getCellName(), subscriptionConfig.getComposedAddress(completeAddress));
				// } else {
				// log.debug("Key={} already exists in the function mapping. Therefore no additional subscription is necessary", key);
				// }
			} catch (Exception e) {
				log.error("Cannot subscribe address={}", this.getCellName() + ":" + subscriptionConfig.getAddress(), e);
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
				this.getCommunicator().unsubscribeDatapoint(subscriptionsConfig.getComposedAddress(this.getCellName()));
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
	public void shutDownFunction() {
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
			log.debug("Agent {}> ==== shut down function={} ====", this.getCell().getName(), this.getFunctionName());
		} catch (Exception e) {
			log.error("No clean shutdown possible", e);
		}
	}

	/**
	 * This function is first executed in case the function shall be closed. It is implemented by the children. However, it should not be used to close the function.
	 * 
	 * @throws Exception
	 */
	protected abstract void shutDownImplementation() throws Exception;

	@Override
	public void updateSubscribedData(final String topic, JsonElement data) {
		// FIXME: Extend the topic to
		// synchronized (this.monitoringObject) {
		String id = topic;
		// Create datapointmapping ID to datapoint with new value
		// Map<String, Datapoint> subscriptions = new HashMap<>();
		Optional<Entry<String, DatapointConfig>> existingId = this.getSubscribedDatapoints().entrySet().stream().filter(e -> e.getValue().getAddress().equals(topic)).findFirst();

		if (existingId.isPresent()) {
			id = existingId.get().getKey();
		}
//			this.getSubscribedDatapoints().forEach((k, v) -> {
//				if (data.containsKey(v.getAddress())) {
//					subscriptions.put(k, data.get(v.getAddress()));
//				}
//			});

		this.updateDatapointsById(id, data);
		// }
	}

	protected abstract void updateDatapointsById(final String id, final JsonElement data);

	@Override
	public String getFunctionName() {
		return this.cellFunctionName;
	}

	@Override
	public String getCellName() {
		return this.getCell().getName();
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

	protected MqttCommunicator getCommunicator() {
		return this.comm;
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

	/**
	 * Get the datapoint builder
	 * 
	 * @return
	 */
	protected DPBuilder getDatapointBuilder() {
		return dpBuilder;
	}

	protected Map<String, Function<Request, Response>> getFunctions() {
		return functions;
	}

}
