package at.tuwien.ict.kore.cell.core.behaviors;

import java.util.List;

import at.tuwien.ict.kore.cell.datastructures.Datapackage;
import at.tuwien.ict.kore.cell.storage.DataStorageSubscriberManager;
import jade.core.behaviours.CyclicBehaviour;

public class SubscriberNotifyBehavior extends CyclicBehaviour implements DataStorageSubscriberManager {

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifySubscribers(List<String> subscribers, String address, Datapackage subscribedData) {
		//Trigger system to send messages to each agent
		
	}

}
