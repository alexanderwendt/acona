package at.tuwien.ict.acona.cell.activator;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface ActivationHandler {
	/**
	 * The internal activations to trigger a behavior
	 * 
	 * @param address
	 * @param subscribedData
	 */
	public void activateLocalBehaviors(Datapoint subscribedData);
	
	public void registerActivatorInstance(String datapointAddress, ActivatorInstance activatorInstance);
	
	public void deregisterActivatorInstance(String datapointAddress, String activatorInstanceName);
	
}
