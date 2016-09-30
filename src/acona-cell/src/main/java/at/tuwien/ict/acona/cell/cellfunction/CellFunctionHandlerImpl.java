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

import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellFunctionHandlerImpl implements CellFunctionHandler {

	private static Logger log = LoggerFactory.getLogger(CellFunctionHandlerImpl.class);
	private final Map<String, List<CellFunction>> cellFunctionMap = new ConcurrentHashMap<String, List<CellFunction>>();
	
	private Cell caller;
	
	@Override
	public void activateLocalFunctions(Datapoint subscribedData) {
		//If there are any functions, then they should be activated
		if (cellFunctionMap.containsKey(subscribedData.getAddress())) {
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
	}

	@Override
	public void registerCellFunctionInstance(CellFunction cellFunctionInstance) {
		//Get all subscribed addresses
		List<DatapointConfig> activatorAddresses = new ArrayList<DatapointConfig>(cellFunctionInstance.getSubscribedDatapoints().values());
		
		//Go through each address and add the activator to this address
		activatorAddresses.forEach(subscriptionConfig->{
			if (this.cellFunctionMap.containsKey(subscriptionConfig.getAddress())==false) {
				//Add new entry
				List<CellFunction> activators = new LinkedList<CellFunction>();
				activators.add(cellFunctionInstance);
				this.cellFunctionMap.put(subscriptionConfig.getAddress(), activators);
				
				log.info("Address={}, registered activator={} in agent{}", subscriptionConfig.getAddress(), cellFunctionInstance, this.caller.getLocalName());
			} else if (this.cellFunctionMap.get(subscriptionConfig.getAddress()).contains(cellFunctionInstance)==false) {
				this.cellFunctionMap.get(subscriptionConfig.getAddress()).add(cellFunctionInstance);
				log.info("Address={}, added activator={}", subscriptionConfig.getAddress(), cellFunctionInstance);
			} else {
				log.warn("Address={}: Cannot register activator={}. Instance already exists", subscriptionConfig, cellFunctionInstance);
			}
			
			try {
				String agentName = this.caller.getLocalName();
				if (subscriptionConfig.getAgentid()!="") {
					agentName = subscriptionConfig.getAgentid();
				}
				this.caller.getCommunicator().subscribe(Arrays.asList(subscriptionConfig.getAddress()), agentName);
			} catch (Exception e) {
				log.error("Cannot subscribe address={}", subscriptionConfig, e);
			}
		});
		
	}

	@Override
	public void deregisterActivatorInstance(CellFunction activatorInstance) {
		//Deregister activator -> deregister all datapoints in the datastorage itself
		//activatorInstance.setExit();
		
		//Get all subscribed addresses
		List<DatapointConfig> activatorAddresses = new ArrayList<DatapointConfig>(activatorInstance.getSubscribedDatapoints().values());
				
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
