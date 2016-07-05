package at.tuwien.ict.acona.cell.activator;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface ActivationHandler {
	
	//public void init(Cell caller);
	
	/**
	 * Test if behaviors can be activated. The internal activations to trigger a behavior
	 * 
	 * @param address
	 * @param subscribedData
	 */
	public void activateLocalBehaviours(Datapoint subscribedData);
	
	/**
	 * Register an activator that is linked to datapoints through its activations
	 * 
	 * @param activatorInstance
	 */
	public void registerActivatorInstance(Activator activatorInstance);
	
	/**
	 * Deregister an activator instance that is linked to datapoints through its activations
	 * 
	 * @param activatorInstanceName
	 */
	public void deregisterActivatorInstance(Activator activatorInstanceName);
	
}
