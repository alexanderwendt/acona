package at.tuwien.ict.kore.cell.storage;

import java.util.List;

import at.tuwien.ict.kore.cell.datastructures.Datapackage;

public interface DataStorageSubscriberManager {
	public void notifySubscribers(List<String> subscribers, String address, Datapackage subscribedData);
}
