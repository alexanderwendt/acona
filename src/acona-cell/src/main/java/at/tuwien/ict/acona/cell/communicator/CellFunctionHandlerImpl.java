package at.tuwien.ict.acona.cell.communicator;

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

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellFunctionHandlerImpl implements CellFunctionHandler {

	private static Logger log = LoggerFactory.getLogger(CellFunctionHandlerImpl.class);
	//The datapoint activation map consists of agent:datapointaddress
	private final Map<String, List<CellFunction>> datapointActivationMap = new ConcurrentHashMap<>();

	private final Map<String, CellFunction> cellFunctionsMap = new ConcurrentHashMap<>();

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
			Map<String, Datapoint> subscribedDatapointMap = new HashMap<>();
			//FIXME: The function itself does not know from which agent the value arrives
			subscribedDatapointMap.put(subscribedData.getAddress(), subscribedData);

			// FIXME: Sometimes, the instancelist is empty, after keys have been
			// deleted. Consider that

			// run all activations of that datapoint in parallel
			log.trace("Activation dp={}, instancelist={}", subscribedData, instanceList);
			instanceList.forEach((CellFunction a) -> {
				try {
					a.updateSubscribedData(subscribedDatapointMap, callerAgent);
				} catch (Exception e) {
					log.error("Cannot test activation of activator {} and subscription {}", a, subscribedData, e);
				}
			});
		}
	}

	private synchronized void updateValuesOfSubscriber(CellFunction function, Datapoint subscribedData, String callerAgent) throws Exception {
		Map<String, Datapoint> subscribedDatapointMap = new HashMap<>();
		subscribedDatapointMap.put(subscribedData.getAddress(), subscribedData);
		function.updateSubscribedData(subscribedDatapointMap, callerAgent);
	}

	@Override
	public void registerCellFunctionInstance(CellFunction cellFunctionInstance) throws Exception {

		try {
			// Get all subscribed addresses
			synchronized (this) {
				// Add the cellfunction itself
				this.cellFunctionsMap.put(cellFunctionInstance.getFunctionName(), cellFunctionInstance);

				List<DatapointConfig> activatorAddresses = new ArrayList<>(cellFunctionInstance.getSubscribedDatapoints().values());

				// Create a responder to the cellfunction if it is set in the
				if (cellFunctionInstance.getFunctionConfig().getGenerateReponder().getAsBoolean() == true) {
					this.caller.getCommunicator().createResponderForFunction(cellFunctionInstance);
				}

				// Go through each address and add the activator to this address
				for (DatapointConfig subscriptionConfig : activatorAddresses) {
					try {
						//Adds the subscription to the handler
						//Subscribe the datapoint		
						//Construct name (if only "", then use the local agent)
						String agentName = subscriptionConfig.getAgentid(this.caller.getLocalName());
						String key = agentName + ":" + subscriptionConfig.getAddress();
						if (this.datapointActivationMap.containsKey(key) == false) {
							List<Datapoint> initialValue = this.caller.getCommunicator().subscribe(agentName, Arrays.asList(subscriptionConfig.getAddress()));
							log.debug("Subscription request sent to agent={}, address={} and acknowledge received", agentName, subscriptionConfig.getAddress());
						} else {
							log.debug("Key={} already exists in the function mapping. Therefore no additional subscription is necessary", key);
						}
						//Add subscription to the function handler
						this.addSubscription(cellFunctionInstance, subscriptionConfig);
					} catch (Exception e) {
						log.error("Cannot subscribe address={}", subscriptionConfig.getAddress());
						throw new Exception(e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot register cell function " + cellFunctionInstance + ".", e);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	public void deregisterActivatorInstance(CellFunction activatorInstance) throws Exception {
		// Deregister activator -> deregister all datapoints in the datastorage
		// itself
		try {
			synchronized (this) {
				// Get all subscribed addresses
				List<DatapointConfig> activatorAddresses = new ArrayList<>(activatorInstance.getSubscribedDatapoints().values());

				// Go through each address and remove the activator to this address
				activatorAddresses.forEach(subscriptionsConfig -> {
					try {
						//this.removeSubscription(activatorInstance.getFunctionName(), subscriptionsConfig.getAgentid(), subscriptionsConfig.getAddress());
						this.removeSubscription(activatorInstance, subscriptionsConfig);

						String key = subscriptionsConfig.getAgentid(this.caller.getLocalName()) + ":" + subscriptionsConfig.getAddress();
						if (this.datapointActivationMap.containsKey(key) == false) {
							this.caller.getCommunicator().unsubscribe(subscriptionsConfig.getAgentid(this.caller.getLocalName()), Arrays.asList(subscriptionsConfig.getAddress()));
							log.debug("Datapoint {}:{} was deregistered as no functions subscribes it", key);
						} else {
							log.debug("Datapoint {}:{} was deregistered for the function {}. It is still subscribed by the agent.", key);
						}

					} catch (Exception e) {
						log.error("Cannot unsubscribe address={}", subscriptionsConfig.getAddress(), e);
					}

				});

				// Remove the cellfunction itself
				this.cellFunctionsMap.remove(activatorInstance.getFunctionName());

			}
		} catch (Exception e) {
			log.error("Cannot deregister cell function " + activatorInstance + " completely.", e);
			throw new Exception(e.getMessage());
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
	public void addSubscription(CellFunction cellFunctionInstance, DatapointConfig subscriptionConfig) throws Exception {
		//Tasks:
		//1. Check if the destination agent name is this agent or nor
		//2. Subscribe the value and receive the current value of the datapoint
		//3. Add the subscription to the activator to get notified if something changes

		if (cellFunctionInstance == null) {
			throw new Exception("No function available for the subscription");
		}

		//String functionName = cellFunctionInstance.getFunctionName();
		String destinationAgent = subscriptionConfig.getAgentid(this.caller.getLocalName());
		String address = subscriptionConfig.getAddress();

		//CellFunction cellFunctionInstance = this.getCellFunction(functionName);

		//Construct name (if only "", then use the local agent)
		String agentName = destinationAgent;
		if (agentName == null || agentName.isEmpty() || agentName.equals("")) {
			agentName = this.caller.getLocalName();
		}

		//TODO: Figure out how to proceed with the read initial value
		//		if (initialValue.isEmpty()==false) {
		//			this.updateValuesOfSubscriber();
		//		} else {
		//			throw new Exception("Something went wrong with the initial read of the subscription");
		//		}

		//Generate key for the internal activator
		String key = agentName + ":" + address;

		if (this.datapointActivationMap.containsKey(key) == false) {
			// Add new entry
			List<CellFunction> activators = new LinkedList<>();
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
	public void removeSubscription(CellFunction cellFunctionInstance, String address, String agentid) throws Exception {
		this.removeSubscription(cellFunctionInstance, DatapointConfig.newConfig("unsubscribe", address, agentid, SyncMode.SUBSCRIBEONLY));
	}

	@Override
	public void removeSubscription(CellFunction cellFunctionInstance, DatapointConfig subscription) throws Exception {
		//CellFunction cellFunctionInstance = this.getCellFunction(functionName);

		if (cellFunctionInstance == null) {
			throw new Exception("No function available for the subscription");
		}

		//Construct name
		String agentName = subscription.getAgentid(this.caller.getLocalName());

		String key = agentName + ":" + subscription.getAddress();

		try {
			if (this.datapointActivationMap.containsKey(key) == true) {
				if (this.datapointActivationMap.get(key).contains(cellFunctionInstance)) {
					this.datapointActivationMap.get(key).remove(cellFunctionInstance);
				}

				if (this.datapointActivationMap.get(key).isEmpty() == true) {
					this.datapointActivationMap.remove(key);
					log.warn("Empty key found in the activator of datapoints. Key={}, cell function={}", key, cellFunctionInstance);
				}

				log.info("unsubscribed datapoint address={} for function={}, ", key, cellFunctionInstance);
			} else {
				throw new Exception("The datapoint activatormap does not contain the address " + key);
			}

		} catch (Exception e) {
			log.error("Address={}: Cannot deregister activator={}", key, cellFunctionInstance, e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public List<String> getCellFunctionNames() {
		return new ArrayList<>(this.cellFunctionsMap.keySet());
	}

}
