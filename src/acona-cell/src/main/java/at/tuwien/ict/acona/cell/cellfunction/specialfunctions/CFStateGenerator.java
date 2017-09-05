package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.communicator.CellFunctionHandlerListener;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CFStateGenerator extends CellFunctionImpl implements CellFunctionHandlerListener {

	private static final Logger log = LoggerFactory.getLogger(CFStateGenerator.class);
	public final static String SYSTEMSTATEADDRESS = "systemstate";

	private List<String> currentlyRegisteredFunctions = new ArrayList<>();
	private Map<String, ServiceState> currentStates = new HashMap<>();
	private Map<String, String> currentDescriptions = new HashMap<>();

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		log.info("Functions={}", this.currentlyRegisteredFunctions);
		return null;
	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		//log.info("============ Message update =============");
		try {
			data.forEach((k, v) -> {
				ServiceState state = ServiceState.valueOf(v.getValue().getAsString());
				this.currentStates.put(k, state);
			});

			this.generateSystemState();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//log.warn("State update={}", data);
	}

	private void generateSystemState() throws Exception {
		Chunk systemState = Chunk.newChunk("SystemState", "SYSTEMSTATE")
				.setValue("agentname", this.getAgentName())
				.setValue("hasDescription", "ACONA Cell");

		this.currentStates.forEach((k, v) -> {
			try {
				systemState.addAssociatedContent("hasFunction", Chunk.newChunk(k, "STATE")
						.setValue("hasState", v.toString())
						.setValue("hasDescription", this.currentDescriptions.get(k)));
			} catch (Exception e) {
				log.error("Cannot add state to system state", e);
			}
		});

		this.writeLocal(Datapoints.newDatapoint(SYSTEMSTATEADDRESS).setValue(systemState.toJsonObject()));
		log.debug("Current system state={}", systemState);

	}

	@Override
	protected void cellFunctionInit() throws Exception {
		//Register this function to get notified if new functions are registered or deregistered.
		this.currentlyRegisteredFunctions = this.getCell().getFunctionHandler().registerLister(this);
		this.currentlyRegisteredFunctions.forEach((f) -> {
			try {
				this.initializeFunction(f);
			} catch (Exception e) {
				log.error("Cannot init state monitor function", e);
			}

		});
	}

	private void initializeFunction(String name) throws Exception {
		this.addManagedDatapoint(DatapointConfig.newConfig(name, name + ".state", SyncMode.SUBSCRIBEONLY));
		String state = this.getCommunicator().read(name + ".state").getValue().getAsString();

		this.currentStates.put(name, ServiceState.valueOf(state));

		String description = this.getCommunicator().read(name + "." + CellFunctionThreadImpl.DESCRIPTIONSUFFIX).getValue().getAsString();
		this.currentDescriptions.put(name, description);

		this.generateSystemState();
		log.info("subscriptions={}", this.getCell().getSubscriptionHandler().getCellFunctionDatapointMapping());
		log.debug("Available functions={}, service state={}", name, state);
	}

	private void removeFunction(String name) throws Exception {
		this.currentlyRegisteredFunctions.remove(name);
		this.currentDescriptions.remove(name);
		this.currentStates.remove(name);

		this.generateSystemState();
		//TODO: Add unsubscribe

	}

	@Override
	protected void shutDownImplementation() throws Exception {
		//Unregister this function from the function handler listeners
		this.getCell().getFunctionHandler().unregisterListener(this);

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
		return this.getFunctionName();
	}

}
