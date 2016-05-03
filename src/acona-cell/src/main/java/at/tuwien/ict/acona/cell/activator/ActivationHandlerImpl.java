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
	private final Map<String, List<ActivatorInstance>> activatorMap = new ConcurrentHashMap<String, List<ActivatorInstance>>();
	
	private Cell caller;
	
	@Override
	public void activateLocalBehaviors(Datapoint subscribedData) {
		List<ActivatorInstance> instanceList = activatorMap.get(subscribedData.getAddress());
		
		//run all activations of that datapoint in parallel
		instanceList.forEach((ActivatorInstance a)->a.runActivation(subscribedData));
	}

	@Override
	public void registerActivatorInstance(String address, ActivatorInstance activatorInstance) {
		if (this.activatorMap.containsKey(address)==false) {
			//Add new entry
			List<ActivatorInstance> activators = new LinkedList<ActivatorInstance>();
			activators.add(activatorInstance);
			this.activatorMap.put(address, activators);
			
			log.info("Address={}, registered first activator={}", address, activatorInstance);
		} else if (this.activatorMap.get(address).contains(activatorInstance)==false) {
			this.activatorMap.get(address).add(activatorInstance);
			log.info("Address={}, added activator={}", address, activatorInstance);
		} else {
			log.warn("Address={}: Cannot register activator={}. Instance already exists", address, activatorInstance);
		}
	}

	@Override
	public void deregisterActivatorInstance(String address, ActivatorInstance activatorInstanceName) {
		if (this.activatorMap.containsKey(address)==true) {
			this.activatorMap.get(address).remove(activatorInstanceName);
			if (this.activatorMap.get(address).isEmpty()==true) {
				this.activatorMap.remove(address);
			}
			
			log.info("Address={}, deregistered activator={}", address, activatorInstanceName);
		} else {
			log.warn("Address={}: Cannot deregister activator={}", address, activatorInstanceName);
		}
		
	}

	@Override
	public void init(Cell caller) {
		this.caller = caller;
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActivationHandler: activatorMap=");
		builder.append(activatorMap);
		return builder.toString();
	}

}
