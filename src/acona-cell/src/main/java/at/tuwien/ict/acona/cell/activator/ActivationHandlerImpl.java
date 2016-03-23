package at.tuwien.ict.acona.cell.activator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ActivationHandlerImpl implements ActivationHandler {

	private Map<String, ActivatorInstance> activatorMap = new ConcurrentHashMap<String, ActivatorInstance>();
	
	@Override
	public void activateLocalBehaviors(Datapoint subscribedData) {
		ActivatorInstance instance = activatorMap.get(subscribedData.getAddress());
		
		instance.runActivation(subscribedData);
	}

	@Override
	public void registerActivatorInstance(String address, ActivatorInstance activatorInstance) {
		this.activatorMap.put(address, activatorInstance);
		
	}

	@Override
	public void deregisterActivatorInstance(String address, String activatorInstanceName) {
		// TODO Auto-generated method stub
		
	}

}
