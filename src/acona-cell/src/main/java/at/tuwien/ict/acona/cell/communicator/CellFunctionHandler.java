package at.tuwien.ict.acona.cell.communicator;

import java.util.List;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.core.Cell;

/**
 * This class contains the instance references to all cell functions in a cell
 * 
 * @author wendt
 *
 */
public interface CellFunctionHandler {

	public void init(Cell caller);

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
	public void deregisterActivatorInstance(String activatorInstanceName) throws Exception;

	public CellFunction getCellFunction(String functionName);

	public List<String> getCellFunctionNames();

}
