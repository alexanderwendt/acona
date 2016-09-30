package at.tuwien.ict.acona.cell.cellfunction;

import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface CellFunctionHandler {
	
	public void init(Cell caller);
	
	/**
	 * Test if behaviors can be activated. The internal activations to trigger a behavior
	 * 
	 * @param address
	 * @param subscribedData
	 */
	public void activateLocalFunctions(Datapoint subscribedData);
	
	/**
	 * Register an activator that is linked to datapoints through its activations
	 * 
	 * @param activatorInstance
	 */
	public void registerCellFunctionInstance(CellFunction activatorInstance);
	
	/**
	 * Deregister an activator instance that is linked to datapoints through its activations
	 * 
	 * @param activatorInstanceName
	 */
	public void deregisterActivatorInstance(CellFunction activatorInstanceName);
	
	public Map<String, List<CellFunction>> getCellFunctionMapping();
	
}
