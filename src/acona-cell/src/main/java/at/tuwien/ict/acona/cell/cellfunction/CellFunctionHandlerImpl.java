package at.tuwien.ict.acona.cell.cellfunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellFunctionHandlerImpl implements CellFunctionHandler {

	private static Logger log = LoggerFactory.getLogger(CellFunctionHandlerImpl.class);
	//The datapoint activation map consists of agent:datapointaddress
	private final Map<String, List<CellFunction>> datapointActivationMap = new ConcurrentHashMap<String, List<CellFunction>>();
	private final Map<String, CellFunction> cellFunctionsMap = new ConcurrentHashMap<String, CellFunction>();

	private Cell caller;

	@Override
	public synchronized void activateNotifySubscribers(String callerAgent, Datapoint subscribedData) {
		//Construct key
		String key = callerAgent + ":" + subscribedData.getAddress();

		// If there are any functions, then they should be activated
		if (datapointActivationMap.containsKey(key)) {
			// Get all instances, which subscribe the datapoint
			List<CellFunction> instanceList = datapointActivationMap.get(key);
			// Add datapoint to map
			Map<String, Datapoint> subscribedDatapointMap = new HashMap<String, Datapoint>();
			//FIXME: The function itself does not know from which agent the value arrives
			subscribedDatapointMap.put(subscribedData.getAddress(), subscribedData);

			// FIXME: Sometimes, the instancelist is empty, after keys have been
			// deleted. Consider that

			// run all activations of that datapoint in parallel
			log.trace("Activation dp={}, instancelist={}", subscribedData, instanceList);
			instanceList.forEach((CellFunction a) -> {
				try {
					a.updateSubscribedData(subscribedDatapointMap);
				} catch (Exception e) {
					log.error("Cannot test activation of activator {} and subscription {}", a, subscribedData, e);
				}
			});
		}
	}

	@Override
	public void registerCellFunctionInstance(CellFunction cellFunctionInstance) {
		// Get all subscribed addresses
		synchronized (this) {
			// Add the cellfunction itself
			this.cellFunctionsMap.put(cellFunctionInstance.getFunctionName(), cellFunctionInstance);

			List<DatapointConfig> activatorAddresses = new ArrayList<DatapointConfig>(cellFunctionInstance.getSubscribedDatapoints().values());

			// Create a responder to the cellfunction if it is set in the
			if (cellFunctionInstance.getFunctionConfig().getGenerateReponder().getAsBoolean() == true) {
				this.caller.getCommunicator().createResponderForFunction(cellFunctionInstance);
			}

			// Go through each address and add the activator to this address
			activatorAddresses.forEach(subscriptionConfig -> {
				try {
					this.addSubscription(cellFunctionInstance.getFunctionName(), subscriptionConfig.getAgentid(), subscriptionConfig.getAddress());

					//				if (this.datapointActivationMap.containsKey(subscriptionConfig.getAddress()) == false) {
					//					// Add new entry
					//					List<CellFunction> activators = new LinkedList<CellFunction>();
					//					activators.add(cellFunctionInstance);
					//					this.datapointActivationMap.put(subscriptionConfig.getAddress(), activators);
					//
					//					log.info("Address={}, registered activator={} in agent{}", subscriptionConfig.getAddress(),
					//							cellFunctionInstance, this.caller.getLocalName());
					//				} else if (this.datapointActivationMap.get(subscriptionConfig.getAddress())
					//						.contains(cellFunctionInstance) == false) {
					//					this.datapointActivationMap.get(subscriptionConfig.getAddress()).add(cellFunctionInstance);
					//					log.info("Address={}, added activator={}", subscriptionConfig.getAddress(), cellFunctionInstance);
					//				} else {
					//					log.warn("Address={}: Cannot register activator={}. Instance already exists", subscriptionConfig,
					//							cellFunctionInstance);
					//				}

					// Register subscribed datapoints
					String agentName = this.caller.getLocalName();
					if (subscriptionConfig.getAgentid() != "") {
						agentName = subscriptionConfig.getAgentid();
					}

					this.caller.getCommunicator().subscribe(Arrays.asList(subscriptionConfig.getAddress()), agentName);

				} catch (Exception e) {
					log.error("Cannot subscribe address={}", subscriptionConfig.getAddress(), e);
				}
			});
		}
	}

	@Override
	public void deregisterActivatorInstance(CellFunction activatorInstance) {
		// Deregister activator -> deregister all datapoints in the datastorage
		// itself
		// activatorInstance.setExit();

		synchronized (this) {
			// Get all subscribed addresses
			List<DatapointConfig> activatorAddresses = new ArrayList<DatapointConfig>(activatorInstance.getSubscribedDatapoints().values());

			// Go through each address and remove the activator to this address
			activatorAddresses.forEach(subscriptionsConfig -> {
				try {
					this.removeSubscription(activatorInstance.getFunctionName(), subscriptionsConfig.getAgentid(), subscriptionsConfig.getAddress());

					//				if (this.datapointActivationMap.containsKey(subscriptionsConfig.getAddress()) == true) {
					//					this.datapointActivationMap.get(subscriptionsConfig.getAddress()).remove(activatorInstance);
					//					if (this.datapointActivationMap.get(subscriptionsConfig.getAddress()).isEmpty() == true) {
					//						this.datapointActivationMap.remove(subscriptionsConfig.getAddress());
					//
					//					}
					//
					//					log.info("Address={}, deregistered activator={}", subscriptionsConfig.getAddress(), activatorInstance);
					//				} else {
					//					log.error("Address={}: Cannot deregister activator={}", subscriptionsConfig.getAddress(), activatorInstance);
					//				}
					this.caller.getCommunicator().unsubscribe(Arrays.asList(subscriptionsConfig.getAddress()), subscriptionsConfig.getAgentid());
				} catch (Exception e) {
					log.error("Cannot unsubscribe address={}", subscriptionsConfig.getAddress(), e);
				}

			});

			// Remove the cellfunction itself
			this.cellFunctionsMap.remove(activatorInstance.getFunctionName());

		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Registered functions=");
		builder.append(this.cellFunctionsMap.keySet());
		return builder.toString();
	}

	@Override
	public Map<String, List<CellFunction>> getCellFunctionDatapointMapping() {
		return Collections.unmodifiableMap(datapointActivationMap);
	}

	@Override
	public void init(Cell caller) {
		this.caller = caller;

	}

	@Override
	public CellFunction getCellFunction(String functionName) {
		return this.cellFunctionsMap.get(functionName);
	}

	@Override
	public void addSubscription(String functionName, String destinationAgent, String address) throws Exception {
		CellFunction cellFunctionInstance = this.getCellFunction(functionName);

		if (cellFunctionInstance == null) {
			throw new Exception("No function available for the subscription");
		}

		//Construct name
		String agentName = destinationAgent;
		if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
			agentName = this.caller.getLocalName();
		}

		String key = agentName + ":" + address;

		if (this.datapointActivationMap.containsKey(key) == false) {
			// Add new entry
			List<CellFunction> activators = new LinkedList<CellFunction>();
			activators.add(cellFunctionInstance);
			this.datapointActivationMap.put(key, activators);

			log.info("Address={}, registered activator={} in agent{}", key, cellFunctionInstance, this.caller.getLocalName());
		} else if (this.datapointActivationMap.get(key).contains(cellFunctionInstance) == false) {
			this.datapointActivationMap.get(key).add(cellFunctionInstance);
			log.info("Address={}, added activator={}", key, cellFunctionInstance);
		} else {
			log.warn("Address={}: Cannot register activator={}. Instance already exists", key, cellFunctionInstance);
		}

	}

	@Override
	public void removeSubscription(String functionName, String destinationAgent, String address) throws Exception {
		CellFunction cellFunctionInstance = this.getCellFunction(functionName);

		if (cellFunctionInstance == null) {
			throw new Exception("No function available for the subscription");
		}

		//Construct name
		String agentName = destinationAgent;
		if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
			agentName = this.caller.getLocalName();
		}

		String key = agentName + ":" + address;

		try {
			if (this.datapointActivationMap.containsKey(key) == true) {
				this.datapointActivationMap.get(key).remove(cellFunctionInstance);
				if (this.datapointActivationMap.get(key).isEmpty() == true) {
					this.datapointActivationMap.remove(key);

				}

				log.info("Address={}, deregistered activator={}", key, cellFunctionInstance);
			} else {
				throw new Exception("The datapoint activatormap does not contain the address " + key);
			}

		} catch (Exception e) {
			log.error("Address={}: Cannot deregister activator={}", key, cellFunctionInstance, e);
		}

	}

}
