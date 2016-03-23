package at.tuwien.ict.acona.cell.storage;

import java.util.List;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface DataStorageSubscriberNotificator {
	public void notifySubscribers(List<String> subscribers, String caller, Datapoint subscribedData);
}
