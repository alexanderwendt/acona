package at.tuwien.ict.acona.cell.storage;

import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface DataStorage {
	
	public DataStorage init(DataStorageSubscriberNotificator subscriberManager);
	
	public void write(Datapoint datapackage, String caller);
	public void add(Datapoint datapackage, String caller);
	public void remove(String address, String caller);
	public Datapoint read(String address);
	
	public void subscribeDatapoint(String address, String caller);
	public void unsubscribeDatapoint(String address, String caller);
	
	public Map<String, List<String>> getSubscribers();
}
