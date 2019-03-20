package at.tuwien.ict.acona.mq.core.agentfunction;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.communication.MqttCommunicatorImpl;
import at.tuwien.ict.acona.mq.core.config.AgentFunctionConfig;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class AgentCommunicatorFunctionImpl implements AgentFunction {

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
	private AgentFunctionConfig config;

	/**
	 * Communicator
	 */
	private MqttCommunicator comm;

	/**
	 * Datapoint builder utility
	 */
	private final DPBuilder dpBuilder = new DPBuilder();

	/**
	 * Name of the activator
	 */
	private String cellFunctionName;

	private String functionRootAddress;

	private String host = "tcp://127.0.0.1:1883";
	private String username = "acona";
	private String password = "acona";

	@Override
	public void init(AgentFunctionConfig config, Cell cell) throws Exception {
		try {
			// === Extract fundamental settings ===//
			// Extract settings
			this.config = config;
			this.cell = cell;

			// Get the settings but set also default values
			// Get name
			this.cellFunctionName = this.config.getName();

			this.functionRootAddress = this.dpBuilder.generateAgentTopic(this.getAgentName()) + "/" + this.cellFunctionName;
			log.debug("{}>Root address={}", this.cellFunctionName, this.functionRootAddress);

			log.trace("Initialize special communications functions for an agent with config:{}", config);

			// Create and initialize the communicator
			this.comm = new MqttCommunicatorImpl(this.cell.getDataStorage());
			this.comm.init(host, username, password, this);

			// === Internal init ===//
			// this.setServiceState(ServiceState.INITIALIZING);

			// Possibility to add more subscriptions and to overwrite default
			// Init internal services and datapoints
			// this.initServiceDatapoints();

			// Init the child function
			// this.cellFunctionInit();

			// Get subscriptions from config and add to subscription list
			// this.getFunctionConfig().getManagedDatapoints().forEach(s -> {
			// this.addManagedDatapoint(s);
			// });

			// === Register in the function handler ===//
			this.cell.getFunctionHandler().registerCellFunctionInstance(this); // Here are also the default subscriptions

			// === Register subscriptions === //
			// this.subscribeDatapoints();

		} catch (Exception e) {
			log.error("Cannot init function with config={}", config, e);
			// this.shutDown();
			// this.setServiceState(ServiceState.ERROR);
			throw new Exception(e.getMessage());
		}

		// this.setServiceState(ServiceState.FINISHED);

		log.debug("Function={} in cell={} initialized.", this.getFunctionName(), this.getAgentName());

	}

	@Override
	public String getFunctionName() {
		return this.cellFunctionName;
	}

	@Override
	public String getAgentName() {
		return this.cell.getName();
	}

	@Override
	public Map<String, DatapointConfig> getSubscribedDatapoints() { // ID config
		throw new UnsupportedOperationException();
	}

	@Override
	public AgentFunctionConfig getFunctionConfig() {
		return this.config;
	}

	@Override
	public Response performOperation(String topic, Request param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSubscribedData(String topic, JsonElement data) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutDownFunction() {
		try {
			// Set communication timeouts to small number
			this.getCommunicator().setDefaultTimeout(1);

			// Execute general deregister
			this.cell.getFunctionHandler().deregisterActivatorInstance(this.getFunctionRootAddress());

			// Close communicator
			this.getCommunicator().shutDown();

			// this.getCell().takeDownCell();
			log.debug("Agent {}> ==== shut down function={} ====", this.cell.getName(), this.getFunctionName());
		} catch (Exception e) {
			log.error("No clean shutdown possible", e);
		}

	}

	@Override
	public ServiceState getCurrentState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttCommunicator getCommunicator() {
		return this.comm;
	}

	@Override
	public String getFunctionRootAddress() {
		return this.functionRootAddress;
	}

}
