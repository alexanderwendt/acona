package at.tuwien.ict.acona.cell.storage;

import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface DataStorage {

	public DataStorage init(DataStorageSubscriberNotificator subscriberManager);

	public void write(Datapoint datapackage, String caller) throws Exception;

	public void add(Datapoint datapackage, String caller);

	public void remove(String address, String caller);

	public Datapoint readFirst(String address);

	/**
	 * Reads a datapoint address. Wildcards can be added by using "*". Examples.
	 * read datapoint1.test.test will give exactly one datapoint. datapoint1.*
	 * will give all datapoints, which addresses start with datapoint1.
	 * 
	 * @param address
	 * @return
	 */
	public List<Datapoint> read(String address);

	public void subscribeDatapoint(String address, String caller);

	public void unsubscribeDatapoint(String address, String caller);

	public Map<String, List<String>> getSubscribers();
}
