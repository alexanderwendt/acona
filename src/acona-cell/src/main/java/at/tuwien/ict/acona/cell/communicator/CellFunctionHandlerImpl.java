package at.tuwien.ict.acona.cell.communicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.core.Cell;

public class CellFunctionHandlerImpl implements CellFunctionHandler {

	private static Logger log = LoggerFactory.getLogger(CellFunctionHandlerImpl.class);

	/**
	 * Map with the name of the cellfunction and the actual instance.
	 */
	private final Map<String, CellFunction> cellFunctionsMap = new ConcurrentHashMap<>();
	private Cell hostCell;

	@Override
	public void init(Cell caller) {
		this.hostCell = caller;
	}

	@Override
	public synchronized void registerCellFunctionInstance(CellFunction cellFunctionInstance) throws Exception {

		try {
			// Get all subscribed addresses
			// Add the cellfunction itself
			//Check if this name is already registered
			if (this.cellFunctionsMap.containsKey(cellFunctionInstance.getFunctionName())) {
				log.warn("Agent {}>Cell function={} is already registered. Cellfunction will be overwritten. Watch for errors.", this.hostCell.getLocalName(), cellFunctionInstance.getFunctionName());
			}

			this.cellFunctionsMap.put(cellFunctionInstance.getFunctionName(), cellFunctionInstance);

			// Create a responder to the cellfunction if it is set in the
			if (cellFunctionInstance.getFunctionConfig().getGenerateReponder().getAsBoolean() == true) {
				this.hostCell.getCommunicator().createResponderForFunction(cellFunctionInstance);
				log.info("Agent {}, function {}>Generate external responder to be able to answer incoming messages.", this.hostCell.getLocalName(), cellFunctionInstance.getFunctionName());
			}
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
				log.warn("Agent {}, function {}>WARNING: Function is not present in the function list and cannot be deregistered", this.hostCell.getLocalName(), activatorInstance);
			}

			// Remove the cellfunction itself
			this.cellFunctionsMap.remove(activatorInstance);
		} catch (Exception e) {
			log.error("Cannot deregister cell function " + activatorInstance, e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public CellFunction getCellFunction(String functionName) {
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

}
