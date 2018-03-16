package at.tuwien.ict.acona.cell.communicator;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class SubscriptionHandlerImpl implements SubscriptionHandler {

	private static Logger log = LoggerFactory.getLogger(SubscriptionHandlerImpl.class);

	private String cellName = "";
	private CellFunctionHandler functionHandler = null;

	/**
	 * The datapoint activation map consists of agent:datapointaddress, List<Function names>
	 */
	private final Map<String, List<String>> datapointActivationMap = new ConcurrentHashMap<>(); // FIXME: Use list of cell names instead of cell function to get indepenent. Only in the function mapping the function names are mapped to functions

	@Override
	public void init(CellFunctionHandler functionHandler, String cellName) {
		this.cellName = cellName;
		this.functionHandler = functionHandler;

	}

	@Override
	public void activateNotifySubscribers(String callerAgent, Datapoint subscribedData) {
		// Construct key
		String key = callerAgent + ":" + subscribedData.getAddress();

		// If there are any functions, then they should be activated
		if (datapointActivationMap.containsKey(key)) {
			// Add datapoint to map
			Map<String, Datapoint> subscribedDatapointMap = new HashMap<>();
			// FIXME: The function itself does not know from which agent the value arrives
			subscribedDatapointMap.put(subscribedData.getAddress(), subscribedData);

			// Get all instances, which subscribe the datapoint
			// synchronized (this.datapointActivationMap) {
			List<String> instanceList = datapointActivationMap.get(key);
			// FIXME: Sometimes, the instancelist is empty, after keys have been
			// deleted. Consider that

			// run all activations of that datapoint in parallel
			log.trace("Activation dp={}, instancelist={}", subscribedData, instanceList);
			for (String a : instanceList) {
				try {
					this.functionHandler.getCellFunction(a).updateSubscribedData(subscribedDatapointMap, callerAgent);
				} catch (Exception e) {
					log.error("Cannot test activation of activator {} and subscription {}", a, subscribedData, e);
				}
			}
			// }

		}
	}

	@Override
	public Map<String, List<String>> getCellFunctionDatapointMapping() {
		return Collections.unmodifiableMap(datapointActivationMap);
	}

	@Override
	public void addSubscription(final String cellFunctionInstanceName, final String address) throws Exception {
		// Tasks:
		// 1. Check if the destination agent name is this agent or nor
		// 2. Subscribe the value and receive the current value of the datapoint
		// 3. Add the subscription to the activator to get notified if something changes

		if (cellFunctionInstanceName == null) {
			throw new Exception("No function available for the subscription as the function instance is null.");
		}

		String key = address;

		// synchronized (this.datapointActivationMap) {
		if (this.datapointActivationMap.containsKey(key) == false) {
			// Add new entry
			List<String> activators = new LinkedList<>();
			activators.add(cellFunctionInstanceName);
			this.datapointActivationMap.put(key, activators);

			log.info("Agent={}>, address={}>Registered activator={} in agent{}", this.cellName, key, cellFunctionInstanceName, this.cellName);
		} else if (this.datapointActivationMap.get(key).contains(cellFunctionInstanceName) == false) {
			this.datapointActivationMap.get(key).add(cellFunctionInstanceName);
			log.info("Agent={}, address={}>Added activator={}", this.cellName, key, cellFunctionInstanceName);
		} else {
			log.warn("Agent={}, function={}>WARNING: Subscription will not be registered for address={}. Instance already exists. Will not overwrite it.", this.cellName, cellFunctionInstanceName, key);
		}
		// }
	}

	// @Override
	// public void removeSubscription(String cellFunctionInstance, String key) throws Exception {
	// Datapoint dp = Datapoints.newDatapoint(key);
	// this.removeSubscription(cellFunctionInstance, DatapointConfig.newConfig("unsubscribe", dp.getAddress(), dp.getAgent(), SyncMode.SUBSCRIBEONLY));
	// }

	@Override
	public void removeSubscription(String cellFunctionInstance, String key) throws Exception {
		// Construct name
		// String agentName = subscription.getAgentid(this.cellName);
		// String key = agentName + ":" + subscription.getAddress();

		// String key = subscription.getComposedAddress(this.cellName);

		// synchronized (this) {
		try {
			if (this.datapointActivationMap.containsKey(key) == true) {
				this.datapointActivationMap.remove(key);
				log.info("unsubscribed datapoint address={} for function={}, ", key, cellFunctionInstance);
				// if (this.datapointActivationMap.get(key).contains(cellFunctionInstance)) {
				// this.datapointActivationMap.get(key).remove(cellFunctionInstance);
				// this.datapointActivationMap.remove(key);
				//
				// log.info("unsubscribed datapoint address={} for function={}, ", key, cellFunctionInstance);
				// }

				// if (this.datapointActivationMap.get(key).isEmpty() == true) {
				// log.warn("Empty key found in the activator of datapoints but no function. Key={}. Entry={}, cell function={}", key, this.datapointActivationMap.get(key), cellFunctionInstance);
				// this.datapointActivationMap.remove(key);
				// }

			} else {
				throw new Exception("The datapoint activatormap does not contain the address " + key);
			}

		} catch (Exception e) {
			log.error("Address={}: Cannot deregister activator={}", key, cellFunctionInstance, e);
			throw new Exception(e.getMessage());
		}
		// }
	}

}
