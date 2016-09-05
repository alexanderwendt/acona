package at.tuwien.ict.acona.cell.activator;

import java.util.List;
import java.util.Map;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.core.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface Activator {
	
	/**
	 * @param name
	 * @param subscriptionAddresses
	 * @param logic
	 * @param conditions
	 * @param behavior
	 * @param caller
	 */
	public Activator init(String name, Map<String, List<Condition>> subscriptionCondition, String logic, CellFunctionBehaviour behavior, Cell caller);
	
	/**
	 * run the activatior and test if the subscribed data can activate something
	 * 
	 * @param subscribedData
	 * @return true if the datapoint triggers activation. It returns false if the datapoint does not trigger any activation
	 */
	public boolean runActivation(Datapoint subscribedData);

	/**
	 * Get the name of the activator
	 * 
	 * @return
	 */
	public String getActivatorName();
	
	/**
	 * Get all datapoints, which are subscribed (linked) to this activator
	 * 
	 * @return
	 */
	public List<String> getLinkedDatapoints();
	
	/**
	 * Get the condition mapping
	 * 
	 * @return
	 */
	public Map<String, List<ActivatorConditionManager>> getConditionMapping();
	
	/**
	 * Close the activator
	 */
	public void closeActivator();
	
}
