package at.tuwien.ict.kore.cell.storage;

import at.tuwien.ict.kore.cell.datastructures.Datapackage;

public interface DataStorage {
	
	public void init(DataStorageSubscriberManager subscriberManager);
	
	public void write(String address, Datapackage datapackage, String caller);
	public void add(String address, Datapackage datapackage, String caller);
	public Datapackage read(String address);
	
	public void subscribeDatapoint(String address);
	public void unsubscribeDatapoint(String address);
}
