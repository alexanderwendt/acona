package at.tuwien.ict.acona.mq.cell.cellfunction;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicatorImpl;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * The main implementation of a cell function.
 * 
 * Example of how to implement a service that is executed on demand: private Response methodName(Request req) { log.debug("Increment the number in the request={}", req); Response result = new
 * Response(req);
 *
 * try { int value = req.getParameter("input", Integer.class); value++; result.setResult(new JsonPrimitive(value)); } catch (Exception e) { log.error("Cannot get value to increment");
 * result.setError("Cannot increment string"); } return result; }
 *
 * The method is added to the cell function and communicator in the initialization. This is put in the function specific initialization Map<String, Function<Request, Response>> methods = new
 * HashMap<>(); methods.put("increment", (Request input) -> methodName(input));
 * 
 * @author wendt
 *
 */
public abstract class CellFunctionImpl implements CellFunction {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static String STATESUFFIX = "state";
	public final static String EXTENDEDSTATESUFFIX = "extendedstate";
	public final static String RESULTSUFFIX = "result";
	public final static String DESCRIPTIONSUFFIX = "description";
	public final static String CONFIGSUFFIX = "config";

	/**
	 * Cell, which executes this function
	 */
	private Cell cell;
	/**
	 * Cell function configuration
	 */
	private CellFunctionConfig config;

	/**
	 * Communicator
	 */
	private MqttCommunicator comm;

	/**
	 * Datapoint builder utility
	 */
	private final DPBuilder dpBuilder = new DPBuilder();

	/**
	 * Service Classes for this specific function
	 */
	private final Map<String, Function<Request, Response>> subFunctionsHandlerMap = new ConcurrentHashMap<>();

	/**
	 * Name of the activator
	 */
	private String cellFunctionName;

	private String functionRootAddress;

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

	private String host = "tcp://127.0.0.1:1883";
	private String username = "acona";
	private String password = "acona";
	
	//State variables
	private Request openRequest = null;

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

			this.functionRootAddress = this.dpBuilder.generateCellTopic(this.getCellName()) + "/" + this.cellFunctionName;
			log.debug("{}>Root address={}", this.cellFunctionName, this.functionRootAddress);

			log.trace("Initialize an agent with config:{}", config);

			// Create and initialize the communicator
			this.comm = new MqttCommunicatorImpl(this.cell.getDataStorage());
			this.comm.init(host, username, password, this);

			// === Internal init ===//
			this.setServiceState(ServiceState.INITIALIZING);

			// Possibility to add more subscriptions and to overwrite default
			// Init internal services and datapoints
			this.initServiceDatapoints();

			// Init the child function
			this.cellFunctionInit();

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
			this.setServiceState(ServiceState.ERROR);
			throw new Exception(e.getMessage());
		}

		this.setServiceState(ServiceState.FINISHED);

		log.debug("Function={} in cell={} initialized. Sync datapoints={}", this.getFunctionName(), this.getCellName(), this.managedDatapointConfigs);
	}

	/**
	 * Initialization that shall be executed by the child function
	 * 
	 * @throws Exception
	 */
	protected abstract void cellFunctionInit() throws Exception;

	/**
	 * Initialize function datapoints
	 * 
	 * @throws Exception
	 */
	private void initServiceDatapoints() throws Exception {

		// Add the description handling function
		this.addRequestHandlerFunction(DESCRIPTIONSUFFIX, (Request input) -> getDescription(input));

		// Add configuration handling function
		this.addRequestHandlerFunction(CONFIGSUFFIX, (Request input) -> setConfig(input));

		// Add a start result
		this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.enhanceWithRootAddress(RESULTSUFFIX)).setValue(""));

	}

	/**
	 * Create a description and return to the caller
	 * 
	 * @param req
	 * @return
	 */
	private Response getDescription(Request req) {
		log.debug("Get function description from={}", req);
		Response result = new Response(req);

		try {
			String value = setFunctionDescription();
			result.setResult(new JsonPrimitive(value));
			log.debug("Returned description {}", value);
		} catch (Exception e) {
			log.error("Cannot get value to increment");
			result.setError("Cannot increment string");
		}

		return result;
	}

	/**
	 * Set the description of a function
	 * 
	 * @return
	 */
	protected String setFunctionDescription() {
		return "Service " + this.getFunctionName();
	}

	private Response setConfig(Request req) {
		log.debug("set config from={}", req);
		Response result = new Response(req);

		try {
			result.setError("NOT IMPLEMENTED YET");
		} catch (Exception e) {
			log.error("Cannot set the config");
			result.setError("NOT IMPLEMENTED YET");
		}

		return result;
	}

	/**
	 * Add a function that is triggered by incoming request
	 * 
	 * @param topicSuffix
	 * @param function
	 * @throws Exception
	 */
	protected void addRequestHandlerFunction(String topicSuffix, Function<Request, Response> function) throws Exception {

		// Create the topic
		String topic = this.enhanceWithRootAddress(topicSuffix);

		this.subFunctionsHandlerMap.put(topic, function);

		try {
			this.getCommunicator().subscribeTopic(topic);
		} catch (MqttException e) {
			log.error("Cannot subscribe input to service function {}", this.enhanceWithRootAddress(topicSuffix));
			throw new Exception(e.getMessage());
		}
		log.info("Added function to {}", topic);

	}

	/**
	 * Remove a function from the handler
	 * 
	 * @param topicSuffix
	 * @throws Exception
	 */
	protected void removeRequestHandlerFunction(String topicSuffix) throws Exception {
		String topic = this.functionRootAddress + "/" + topicSuffix;
		this.getCommunicator().unsubscribeTopic(topic);
		this.subFunctionsHandlerMap.remove(topic);

		log.debug("Unsubscribed topic={} and removed its function.", topic);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.tuwien.ict.acona.mq.cell.cellfunction.CellFunction#performOperation(java.lang.String, at.tuwien.ict.acona.mq.datastructures.Request)
	 */
	@Override
	public Response performOperation(String topic, Request param) {
		log.debug("{}>Execute service method={}", this.getFunctionName(), topic);
		Response response=null;
		synchronized (this.subFunctionsHandlerMap) {
			if (this.subFunctionsHandlerMap.containsKey(topic)) {
				response = subFunctionsHandlerMap.get(topic).apply(param);
				//log.debug("Response={}", response);
				
				//Set the state variable if the request is open after the function. If open, the response is null and nothing shall be returned to the caller
				//If not null, the the response shall be returned to the caller. In that way, a request can trigger other functions to complete. Asynchronous calls
				//can be converted into synchronous calls.
				if (response==null) {
					this.setOpenRequest(param);	//Set the current open request as response is null
				} //else {
				//	this.setOpenRequest(null);	//Return a response and therefore set the open request to null
				//}
				
			} else {
				//If no topic could be identified, 
				log.warn("No method for the topic={}. Available methods={}", topic, this.subFunctionsHandlerMap.keySet());
				response = new Response(param);
				response.setError("No method for this topic");
			}
		}
		
		if (response!=null) {
			//Handle errors
			if (response.hasError() == true) {
				log.warn("{}>Execution of service method={} unsuccessful. Error={}.", this.getFunctionName(), topic, response.getError());
			} else {
				log.debug("{}>Execution of service method={} finished.", this.getFunctionName(), topic);
			}
		} else {
			log.debug("The response is open and will be handled by another method.");
		}
		
		
		return response;
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

		// Go through each address and add the activator to this address
		this.getSubscribedDatapoints().values().forEach(subscriptionConfig -> {
			try {
				// Adds the subscription to the handler
				String completeAddress = subscriptionConfig.getAddress();
				// @SuppressWarnings("unused")
				Datapoint initialValue = this.getCommunicator().subscribeDatapoint(completeAddress);
				log.debug("{}>Subscribed address={}.", this.getCellName(), completeAddress);
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
		// Go through each address and remove the activator to this address
		this.getSubscribedDatapoints().values().forEach(subscriptionsConfig -> {
			try {
				this.getCommunicator().unsubscribeDatapoint(subscriptionsConfig.getAddress());
			} catch (Exception e) {
				log.error("Cannot unsubscribe address={}", subscriptionsConfig.getAddress(), e);
			}

		});
	}

	@Override
	public void shutDownFunction() {
		// Unsubscribe all datapoints
		// this.getCell().getFunctionHandler().deregisterActivatorInstance(this);
		try {
			// Set communication timeouts to small number
			//this.getCommunicator().setDefaultTimeout(1);
			
			// Execute specific functions
			this.shutDownImplementation();

			// Remove all subscriptions
			this.removeSubscription();

			// Close communicator
			this.getCommunicator().shutDown();

			// Execute general deregister
			this.getCell().getFunctionHandler().deregisterActivatorInstance(this.getFunctionRootAddress());

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

		this.updateDatapointsById(id, topic, data);
		// }
	}

	protected abstract void updateDatapointsById(final String id, final String address, final JsonElement data);

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

	@Override
	public MqttCommunicator getCommunicator() {
		return this.comm;
	}

	protected <T> T getCustomSetting(String key, Class<T> type) {
		return this.getFunctionConfig().getProperty(cellFunctionName, type);
	}

	protected Cell getCell() {
		return cell;
	}

//	/**
//	 * Return the subscribed datapoint based on its ID in the function
//	 * 
//	 * @param data:
//	 *            inputmap from subscribed data
//	 * @param id:
//	 *            datapoint id defined in the config or in the code
//	 * @return
//	 */
//	protected Datapoint getDatapointFromId(Map<String, Datapoint> data, String id) {
//		return data.get(this.getSyncDatapointConfigs().get(id).getAddress());
//	}

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
		this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.enhanceWithRootAddress(STATESUFFIX)).setValue(serviceState.toString()));
		//this.getCommunicator().publishTopic(this.getDatapointBuilder().newDatapoint(this.enhanceWithRootAddress(STATESUFFIX)).getCompleteAddressAsTopic(""), new JsonPrimitive(serviceState.toString()), true);

		// if (this.getFunctionConfig().getRegisterState().getAsBoolean() == true) {
		// this.getCell().getFunctionHandler().updateState(this, this.currentServiceState);
		// }
		// this.processServiceState();
	}

//	protected void processServiceState() throws Exception {
//
//	}

	/**
	 * Get the datapoint builder
	 * 
	 * @return
	 */
	protected DPBuilder getDatapointBuilder() {
		return dpBuilder;
	}
	
	/**
	 * In the update data method, datapoints are often received as JsonElement. This method converts it to a datapoint and gets the value. Usually, only the value is of interest.
	 * 
	 * @param dp
	 * @return
	 * @throws Exception
	 */
	protected JsonElement getValueFromJsonDatapoint(JsonElement dp) throws Exception {
		JsonElement result = null;
		
		if (dp.isJsonObject()==true) {
			result = this.getDatapointBuilder().toDatapoint(dp.getAsJsonObject()).getValue();
		} else {
			throw new Exception("JsonElement is no JsonObject");
		}
		
		return result;
	}

	protected Map<String, Function<Request, Response>> getSubfunctions() {
		return this.subFunctionsHandlerMap;
	}

	@Override
	public String getFunctionRootAddress() {
		return functionRootAddress;
	}

	protected String enhanceWithRootAddress(String suffix) {
		return this.getFunctionRootAddress() + "/" + suffix;
	}
	
	/**
	 * Get the current ope request to see if there is smething to return
	 * 
	 * @return null if no open request is available, request to get the current open request
	 */
	protected Request getOpenRequest() {
		return openRequest;
	}

	/**
	 * Set the open request as a parameter if the function cannot return an answer. Else the open request is null.
	 * 
	 * @param openRequest
	 */
	protected void setOpenRequest(Request openRequest) {
		this.openRequest = openRequest;
		log.warn("OpenRequest set from {} to {}", this.openRequest, openRequest);
	}
	
	protected void closeOpenRequestWithOK(boolean closeOpenRequest) throws Exception {
		if (this.getOpenRequest()!=null) {
			this.getCommunicator().sendResponseToOpenRequest((new Response(this.getOpenRequest()).setResultOK()));
			
			if (closeOpenRequest==true) {
				this.setOpenRequest(null);
			}
			
		} else {
			log.error("open request is null. Nothing can be returned");
		}
	}

}
