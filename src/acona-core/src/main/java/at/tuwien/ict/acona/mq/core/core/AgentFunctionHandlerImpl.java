package at.tuwien.ict.acona.mq.core.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunction;

public class AgentFunctionHandlerImpl implements AgentFunctionHandler {

	private static Logger log = LoggerFactory.getLogger(AgentFunctionHandlerImpl.class);

	/**
	 * Map with the name of the cellfunction and the actual instance.
	 */
	private final Map<String, AgentFunction> cellFunctionsMap = new ConcurrentHashMap<>();
	private final List<String> applicationFunctions = new ArrayList<>();
	private final List<AgentFunctionHandlerListener> listenerList = new ArrayList<>();
	private Cell hostCell;

	@Override
	public void init(Cell caller) {
		this.hostCell = caller;
	}

	@Override
	public synchronized void registerCellFunctionInstance(AgentFunction cellFunctionInstance) throws Exception {

		try {
			// Get all subscribed addresses
			// Add the cellfunction itself
			// Check if this name is already registered
			if (this.cellFunctionsMap.containsKey(cellFunctionInstance.getFunctionRootAddress())) {
				log.warn("Agent {}>Cell function={} is already registered. Cellfunction will be overwritten. Watch for errors.", this.hostCell.getName(), cellFunctionInstance.getFunctionRootAddress());
			}

			this.cellFunctionsMap.put(cellFunctionInstance.getFunctionRootAddress(), cellFunctionInstance);

			// Set init state
			if ((this.applicationFunctions.contains(cellFunctionInstance.getFunctionRootAddress()) == false)) {
				synchronized (this.applicationFunctions) {
					this.applicationFunctions.add(cellFunctionInstance.getFunctionRootAddress());
				}

				// Notify all listeners that a new function has been registered
				synchronized (this.listenerList) {
					this.listenerList.forEach(l -> {
						if (l.getListenerFunction().equals(cellFunctionInstance.getFunctionRootAddress()) == false) {
							l.notifyAddedFunction(cellFunctionInstance.getFunctionRootAddress());
						}
					});
				}
			}

			// Create a responder to the cellfunction if it is set in the
			//if (cellFunctionInstance.getFunctionConfig().getGenerateReponder().getAsBoolean() == true) {
				// this.hostCell.getCommunicator().createResponderForFunction(cellFunctionInstance);
			//	log.info("Agent {}, function {}>Generate external responder to be able to answer incoming messages.", this.hostCell.getName(), cellFunctionInstance.getFunctionRootAddress());
			//}
		} catch (Exception e) {
			log.error("Cannot register cell function " + cellFunctionInstance + ".", e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public synchronized void deregisterActivatorInstance(String activatorInstance) throws Exception {
		// Deregister activator -> deregister all datapoints in the datastorage
		// itself
		try {
			if (this.cellFunctionsMap.containsKey(activatorInstance) == false) {
				log.warn("Agent {}, function {}>WARNING: Function is not present in the function list and cannot be deregistered", this.hostCell.getName(), activatorInstance);
			}

			// Remove the cellfunction itself
			this.cellFunctionsMap.remove(activatorInstance);

			// if (cellFunctionInstance.getFunctionConfig().getRegisterState().getAsBoolean()==true) {
			if (this.applicationFunctions.contains(activatorInstance)) {
				synchronized (this.applicationFunctions) {
					this.applicationFunctions.remove(activatorInstance);
				}

				synchronized (this.listenerList) {
					this.listenerList.forEach(l -> {
						if (l.getListenerFunction().equals(activatorInstance) == false) {
							l.notifyRemovedFunction(activatorInstance);
						}
					});
				}

			}

			// }

			// Notify all listeners that an existing function has been unregistered

		} catch (Exception e) {
			log.error("Cannot deregister cell function " + activatorInstance, e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public AgentFunction getCellFunction(String functionName) {
		return this.cellFunctionsMap.get(functionName);
	}

	@Override
	public List<String> getCellFunctionNames() {
		return Collections.unmodifiableList(new ArrayList<>(this.cellFunctionsMap.keySet()));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Registered functions=");
		builder.append(this.cellFunctionsMap.keySet());
		return builder.toString();
	}

	@Override
	public List<String> registerLister(AgentFunctionHandlerListener listener) {
		synchronized (this.listenerList) {
			this.listenerList.add(listener);
		}

		log.debug("A listener was registered for the cell functions");

		return Collections.unmodifiableList(applicationFunctions);
	}

	@Override
	public void unregisterListener(AgentFunctionHandlerListener listener) {
		synchronized (this.listenerList) {
			this.listenerList.remove(listener);
		}

		log.debug("A listener was unregistered");

	}

	// @Override
	// public void updateState(CellFunction function, ServiceState state) {
	// //Update this map
	// this.functionStateMap.put(function.getFunctionName(), state);
	// //Update all listeners
	// this.listenerList.forEach(l -> l.notifyStateUpdate(function.getFunctionName(), state));
	// }

}
