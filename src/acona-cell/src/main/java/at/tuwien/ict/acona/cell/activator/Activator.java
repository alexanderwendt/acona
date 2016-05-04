package at.tuwien.ict.acona.cell.activator;

import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.core.CellFunctionBehavior;
import at.tuwien.ict.acona.cell.core.CellImpl;
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
	public void init(String name, Map<String, List<Condition>> subscriptionCondition, String logic, CellFunctionBehavior behavior, CellImpl caller);
	/**
	 * @param subscribedData
	 * @return
	 */
	public boolean runActivation(Datapoint subscribedData);
	/**
	 * @param condition
	 */
//	public void registerCondition(Condition condition);
//	/**
//	 * @param conditon
//	 */
//	public void deregisterCondition(Condition conditon);
//	/**
//	 * @return
//	 */
	public String getName();
}
