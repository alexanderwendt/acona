package at.tuwien.ict.acona.mq.cell.storage;

import at.tuwien.ict.acona.mq.datastructures.Datapoint;

public interface DataStorageSubscriberNotificator {
	public void notifySubscribers(Datapoint subscribedData);
}
