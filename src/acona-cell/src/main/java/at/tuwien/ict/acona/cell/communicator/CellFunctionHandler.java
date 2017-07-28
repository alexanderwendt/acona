package at.tuwien.ict.acona.cell.communicator;

import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface CellFunctionHandler {

	public void init(Cell caller);

	/**
	 * Test if behaviors can be activated. The internal activations to trigger a
	 * behavior
	 * 
	 * @param address
	 * @param subscribedData
	 */
	public void activateNotifySubscribers(String callerAgent, Datapoint subscribedData);

	/**
	 * Register an activator that is linked to datapoints through its
	 * activations
	 * 
	 * @param activatorInstance
	 * @throws Exception
	 */
	public void registerCellFunctionInstance(CellFunction activatorInstance) throws Exception;

	/**
	 * Deregister an activator instance that is linked to datapoints through its
	 * activations
	 * 
	 * @param activatorInstanceName
	 * @throws Exception
	 */
	public void deregisterActivatorInstance(CellFunction activatorInstanceName) throws Exception;

	public void addSubscription(String cellFunctionInstance, DatapointConfig subscriptionConfig) throws Exception;

	public void removeSubscription(String activatorInstance, DatapointConfig subscriptionConfig) throws Exception;

	public void removeSubscription(String cellFunctionInstance, String address, String agentid) throws Exception;

	public Map<String, List<String>> getCellFunctionDatapointMapping();

	public CellFunction getCellFunction(String functionName);

	public List<String> getCellFunctionNames();

}
