package at.tuwien.ict.acona.cell.cellfunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.config.SubscriptionConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellFunctionHandlerImpl implements CellFunctionHandler {

	private static Logger log = LoggerFactory.getLogger(CellFunctionHandlerImpl.class);
	private final Map<String, List<CellFunction>> cellFunctionMap = new ConcurrentHashMap<String, List<CellFunction>>();
	
	private Cell caller;
	
	@Override
	public void activateLocalBehaviours(Datapoint subscribedData) {
		//Get all instances, which subscribe the datapoint
		List<CellFunction> instanceList = cellFunctionMap.get(subscribedData.getAddress());
		//Add datapoint to map
		Map<String, Datapoint> subscribedDatapointMap = new HashMap<String, Datapoint>();
		subscribedDatapointMap.put(subscribedData.getAddress(), subscribedData);
		
		//FIXME: Sometimes, the instancelist is empty, after keys have been deleted. Consider that
		
		//run all activations of that datapoint in parallel
		log.trace("Activation dp={}, instancelist={}", subscribedData, instanceList);
		instanceList.forEach((CellFunction a)->{
			try {
				a.updateData(subscribedDatapointMap);
			} catch (Exception e) {
				log.error("Cannot test activation of activator {} and subscription {}", a, subscribedData, e);
			}
		});
	}

	@Override
	public void registerActivatorInstance(CellFunction activatorInstance) {
		//Get all subscribed addresses
		List<SubscriptionConfig> activatorAddresses = new ArrayList<SubscriptionConfig>(activatorInstance.getSubscribedDatapoints().values());
		
		//Go through each address and add the activator to this address
		activatorAddresses.forEach(subscriptionConfig->{
			if (this.cellFunctionMap.containsKey(subscriptionConfig)==false) {
				//Add new entry
				List<CellFunction> activators = new LinkedList<CellFunction>();
				activators.add(activatorInstance);
				this.cellFunctionMap.put(subscriptionConfig.getAddress(), activators);
				
				log.info("Address={}, registered activator={}", subscriptionConfig.getAddress(), activatorInstance);
			} else if (this.cellFunctionMap.get(subscriptionConfig.getAddress()).contains(activatorInstance)==false) {
				this.cellFunctionMap.get(subscriptionConfig.getAddress()).add(activatorInstance);
				log.info("Address={}, added activator={}", subscriptionConfig.getAddress(), activatorInstance);
			} else {
				log.warn("Address={}: Cannot register activator={}. Instance already exists", subscriptionConfig, activatorInstance);
			}
			
			try {
				this.caller.getCommunicator().subscribe(Arrays.asList(subscriptionConfig.getAddress()), subscriptionConfig.getAgentid());
			} catch (Exception e) {
				log.error("Cannot subscribe address={}", subscriptionConfig);
			}
		});
		
	}

	@Override
	public void deregisterActivatorInstance(CellFunction activatorInstance) {
		//Deregister activator -> deregister all datapoints in the datastorage itself
		activatorInstance.setExit();
		
		//Get all subscribed addresses
		List<SubscriptionConfig> activatorAddresses = new ArrayList<SubscriptionConfig>(activatorInstance.getSubscribedDatapoints().values());
				
		//Go through each address and remove the activator to this address
		activatorAddresses.forEach(subscriptionsConfig->{
			if (this.cellFunctionMap.containsKey(subscriptionsConfig.getAddress())==true) {
				this.cellFunctionMap.get(subscriptionsConfig.getAddress()).remove(activatorInstance);
				if (this.cellFunctionMap.get(subscriptionsConfig.getAddress()).isEmpty()==true) {
					this.cellFunctionMap.remove(subscriptionsConfig.getAddress());
					
				}
				
				log.info("Address={}, deregistered activator={}", subscriptionsConfig, activatorInstance);
			} else {
				log.warn("Address={}: Cannot deregister activator={}", subscriptionsConfig, activatorInstance);
			}
			
			try {
				this.caller.getCommunicator().unsubscribe(Arrays.asList(subscriptionsConfig.getAddress()), subscriptionsConfig.getAgentid());
			} catch (Exception e) {
				log.error("Cannot unsubscribe address={}", subscriptionsConfig);
			}
		});
		
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActivationHandler: activatorMap=");
		builder.append(cellFunctionMap);
		return builder.toString();
	}

	public Map<String, List<CellFunction>> getCellFunctionMapping() {
		return cellFunctionMap;
	}

	@Override
	public void init(Cell caller) {
		this.caller = caller;
		
	}

}
