package at.tuwien.ict.acona.cell.storage.helpers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.storage.DataStorageSubscriberNotificator;

public class DataStorageSubscriberNotificatorMock implements DataStorageSubscriberNotificator {

	protected static Logger log = LoggerFactory.getLogger(DataStorageSubscriberNotificatorMock.class);
	
	private List<SubscriberMock> subscriberInstances = new ArrayList<SubscriberMock>();
	
	@Override
	public void notifySubscribers(List<String> subscribers, String caller, Datapoint subscribedData) {
		log.debug("Notify subscribers={}", subscribers);
		
		subscriberInstances.forEach((SubscriberMock s)->{
			if (subscribers.contains(s.getName())) {
				s.setValue(subscribedData.getValue().getAsString());
			}
		});
	}
	
	public void addSubscriber(SubscriberMock mock) {
		this.subscriberInstances.add(mock);
	}

}
