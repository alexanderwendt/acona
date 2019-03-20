package at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.ServiceState;
import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.core.core.AgentFunctionHandlerListener;
import at.tuwien.ict.acona.mq.datastructures.Chunk;
import at.tuwien.ict.acona.mq.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

/**
 * @author wendt
 * 
 *         Get the state of all functions that are threads in a cell. This functions puts its results in the system state datapoint. At the execution a Json is returned, where each registered function
 *         exists, as well as description and current state [RUNNING, FINISHED, ERROR, INITIALZING]
 *
 */
public class StateMonitor extends AgentFunctionThreadImpl implements AgentFunctionHandlerListener {

	private static final Logger log = LoggerFactory.getLogger(StateMonitor.class);
	public final static String SYSTEMSTATEADDRESS = "systemstate";

	private List<String> currentlyRegisteredFunctions = new ArrayList<>();
	private Map<String, ServiceState> currentStates = new ConcurrentHashMap<>();
	private Map<String, String> currentDescriptions = new ConcurrentHashMap<>();

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// Register this function to get notified if new functions are registered or deregistered.
		this.currentlyRegisteredFunctions = this.getAgent().getFunctionHandler().registerLister(this);
		this.currentlyRegisteredFunctions.forEach((f) -> {
			try {
				this.initializeFunction(f);
			} catch (Exception e) {
				log.error("Cannot init state monitor function", e);
			}
		});
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeFunction() throws Exception {
		this.generateSystemState();
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// log.info("============ Message update =============");
		try {
			log.debug("Received update={}, {}", id, data);
			Datapoint dp = this.getDatapointBuilder().toDatapoint(data.getAsJsonObject());
			ServiceState state = ServiceState.valueOf(dp.getValueAsString());
			this.currentStates.put(id, state);

			this.setStart();

		} catch (Exception e) {
			log.error("Cannot add new system state", e);
		}

		log.trace("system state update finished");
		
	}

	private void generateSystemState() throws Exception {
		ServiceState agentState = ServiceState.FINISHED;
		if (this.currentStates.values().contains(ServiceState.ERROR)) {
			agentState = ServiceState.ERROR;
		} else if (this.currentStates.values().contains(ServiceState.RUNNING)) {
			agentState = ServiceState.RUNNING;
		}

		Chunk systemState = ChunkBuilder.newChunk("SystemState", "SYSTEMSTATE")
				.setValue("agentname", this.getAgentName())
				.setValue("hasState", agentState.toString())
				.setValue("hasDescription", "ACONA Cell");

		StringBuilder runningFunctions = new StringBuilder();
		this.currentStates.forEach((k, v) -> {
			try {
				systemState.addAssociatedContent("hasFunction", ChunkBuilder.newChunk(k, "STATE")
						.setValue("hasState", v.toString())
						.setValue("hasDescription", this.currentDescriptions.get(k)));
				if (v.equals(ServiceState.RUNNING)) {
					runningFunctions.append(k + ",");
				}
			} catch (Exception e) {
				log.error("Cannot add state to system state", e);
			}
		});

		systemState.setValue("runningFunctions", runningFunctions.toString());
		this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(SYSTEMSTATEADDRESS).setValue(systemState.toJsonObject()));
		log.debug("Current system state={}", systemState);

	}

	private void initializeFunction(String functionRootAddress) throws Exception {
		// Add datapoint to managed datapoints
		this.addManagedDatapoint(DatapointConfig.newConfig(functionRootAddress, functionRootAddress + "/state", SyncMode.SUBSCRIBEONLY));
		// Subscribe the datapoint manually from the function to always get the state
		this.getCommunicator().subscribeDatapoint(functionRootAddress + "/state");

		//Datapoint 
		String state = this.getCommunicator().read(functionRootAddress + "/state").getValueOrDefault(new JsonPrimitive(ServiceState.UNDEFINED.toString())).getAsString();

		this.currentStates.put(functionRootAddress, ServiceState.valueOf(state));

		String description = this.getCommunicator().read(functionRootAddress + "." + AgentFunctionThreadImpl.DESCRIPTIONSUFFIX).getValueOrDefault(new JsonPrimitive("No description available.")).getAsString();
		this.currentDescriptions.put(functionRootAddress, description);

		this.generateSystemState();
		// log.info("subscriptions={}", this.getCell().getSubscriptionHandler().getCellFunctionDatapointMapping());
		log.debug("Function={}, state={}", functionRootAddress, state);
	}

	private void removeFunction(String name) throws Exception {
		if (this.currentlyRegisteredFunctions.contains(name)) {
			this.currentlyRegisteredFunctions.remove(name);
		}
		
		this.currentDescriptions.remove(name);
		this.currentStates.remove(name);

		this.generateSystemState();
		// TODO: Add unsubscribe

	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// Unregister this function from the function handler listeners
		this.getAgent().getFunctionHandler().unregisterListener(this);

	}

	@Override
	public void notifyAddedFunction(String registeredFunction) {
		try {
			this.initializeFunction(registeredFunction);
			log.debug("Added function={}", registeredFunction);
		} catch (Exception e) {
			log.error("Cannot add cell function", e);
		}

	}

	@Override
	public void notifyRemovedFunction(String registeredFunction) {
		try {
			this.removeFunction(registeredFunction);
			log.debug("Removed function={}", registeredFunction);
		} catch (Exception e) {
			log.error("Cannot remove function", e);
		}

	}

	@Override
	public String getListenerFunction() {
		return this.getFunctionRootAddress();
	}

}
