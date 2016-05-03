package at.tuwien.ict.acona.cell.activator;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface ActivationHandler {
	
	public void init(Cell caller);
	
	/**
	 * Test if behaviors can be activated. The internal activations to trigger a behavior
	 * 
	 * @param address
	 * @param subscribedData
	 */
	public void activateLocalBehaviors(Datapoint subscribedData);
	
	/**
	 * Register an activator for a certain datapoint address
	 * 
	 * @param datapointAddress
	 * @param activatorInstance
	 */
	public void registerActivatorInstance(String datapointAddress, ActivatorInstance activatorInstance);
	
	/**
	 * Deregister an activator instance for a certain datapoint address
	 * 
	 * @param datapointAddress
	 * @param activatorInstanceName
	 */
	public void deregisterActivatorInstance(String datapointAddress, ActivatorInstance activatorInstanceName);
	
}
