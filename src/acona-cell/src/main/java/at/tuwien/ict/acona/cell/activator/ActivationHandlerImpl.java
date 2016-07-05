package at.tuwien.ict.acona.cell.activator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ActivationHandlerImpl implements ActivationHandler {

	private static Logger log = LoggerFactory.getLogger(ActivationHandlerImpl.class);
	private final Map<String, List<Activator>> activatorMap = new ConcurrentHashMap<String, List<Activator>>();
	
	private Cell caller;
	
	@Override
	public void activateLocalBehaviours(Datapoint subscribedData) {
		List<Activator> instanceList = activatorMap.get(subscribedData.getAddress());
		
		//run all activations of that datapoint in parallel
		log.trace("Test dp={}", subscribedData);
		instanceList.forEach((Activator a)->a.runActivation(subscribedData));
	}

	@Override
	public void registerActivatorInstance(Activator activatorInstance) {
		//Get all subscribed addresses
		List<String> activatorAddresses = activatorInstance.getLinkedDatapoints();
		
		//Go through each address and add the activator to this address
		activatorAddresses.forEach(address->{
			if (this.activatorMap.containsKey(address)==false) {
				//Add new entry
				List<Activator> activators = new LinkedList<Activator>();
				activators.add(activatorInstance);
				this.activatorMap.put(address, activators);
				
				log.info("Address={}, registered activator={}", address, activatorInstance);
			} else if (this.activatorMap.get(address).contains(activatorInstance)==false) {
				this.activatorMap.get(address).add(activatorInstance);
				log.info("Address={}, added activator={}", address, activatorInstance);
			} else {
				log.warn("Address={}: Cannot register activator={}. Instance already exists", address, activatorInstance);
			}
		});
		
	}

	@Override
	public void deregisterActivatorInstance(Activator activatorInstance) {
		//Get all subscribed addresses
		List<String> activatorAddresses = activatorInstance.getLinkedDatapoints();
				
		//Go through each address and remove the activator to this address
		activatorAddresses.forEach(address->{
			if (this.activatorMap.containsKey(address)==true) {
				this.activatorMap.get(address).remove(activatorInstance);
				if (this.activatorMap.get(address).isEmpty()==true) {
					this.activatorMap.remove(address);
				}
				
				log.info("Address={}, deregistered activator={}", address, activatorInstance);
			} else {
				log.warn("Address={}: Cannot deregister activator={}", address, activatorInstance);
			}
		});
		
		
		
	}

//	@Override
//	public void init(Cell caller) {
//		this.caller = caller;
//		
//	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActivationHandler: activatorMap=");
		builder.append(activatorMap);
		return builder.toString();
	}

}
