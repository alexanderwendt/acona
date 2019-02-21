package at.tuwien.ict.acona.cell.communicator;

import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface SubscriptionHandler {

	/**
	 * Init the subscription handler
	 * 
	 * @param functionHandler
	 * @param cellName
	 */
	public void init(CellFunctionHandler functionHandler, String cellName);

	/**
	 * Test if behaviors can be activated. The internal activations to trigger a behavior
	 * 
	 * @param address
	 * @param subscribedData
	 * @throws Exception
	 */
	public void activateNotifySubscribers(String callerAgent, Datapoint subscribedData) throws Exception;

	public void addSubscription(String cellFunctionInstanceName, String key) throws Exception;

	/**
	 * Add new subscription data mapping
	 * 
	 * @param cellFunctionInstanceName
	 * @param subscriptionConfig
	 * @throws Exception
	 */
	// public void addSubscription(String cellFunctionInstanceName, DatapointConfig subscriptionConfig) throws Exception;

	/**
	 * Remove a subscription, e.g. if a function is deregistered and closed
	 * 
	 * @param activatorInstance
	 * @param subscriptionConfig
	 * @throws Exception
	 */
	// public void removeSubscription(String activatorInstance, DatapointConfig subscriptionConfig) throws Exception;

	/**
	 * Remove a subscription, e.g. if a function is deregistered and closed
	 * 
	 * @param cellFunctionInstance
	 * @param address
	 * @param agentid
	 * @throws Exception
	 */
	public void removeSubscription(String cellFunctionInstance, String key) throws Exception;

	/**
	 * Get the mapping of cell function names for a datapoint
	 * 
	 * @return
	 */
	public Map<String, List<String>> getCellFunctionDatapointMapping();

}
