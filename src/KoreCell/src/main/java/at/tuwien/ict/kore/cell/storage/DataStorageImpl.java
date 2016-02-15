package at.tuwien.ict.kore.cell.storage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.cell.datastructures.Datapackage;

public class DataStorageImpl implements DataStorage {
	
	private static Logger log = LoggerFactory.getLogger(DataStorageImpl.class);

	private final Map<String, Datapackage> data = new ConcurrentHashMap<String, Datapackage>();
	private final Map<String, List<String>> subscribers = new ConcurrentHashMap<String, List<String>>();
	private DataStorageSubscriberManager subscriberManager;
	
	
	
	@Override
	public void init(DataStorageSubscriberManager subscriberManager) {
		this.subscriberManager = subscriberManager;
		
	}

	@Override
	public void write(String address, Datapackage datapackage, String caller) {
		log.debug("write address={} data={}", address, datapackage);
		this.data.put(address, datapackage);
	}

	@Override
	public void add(String address, Datapackage datapackage, String caller) {
		throw new UnsupportedOperationException("Method not implemented yet");
		//this.data.merge(address, datapackage, Datapackage::);
		//this.data.mer
		
	}

	@Override
	public Datapackage read(String address) {
		return this.data.get(address);
	}
	
	private void notifySubscribers(String address, Datapackage datapackage, String caller) {
		//Get all subscribers for this datapoint
		final List<String> subscribers = this.subscribers.get(address);
		
		//Remove the caller from the subscibers to be notified. The system shall not notify itself
		subscribers.remove(caller);
		
		//Trigger subscriber behavior
		this.subscriberManager.notifySubscribers(subscribers, address, datapackage);
		
	}

	@Override
	public void subscribeDatapoint(String address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsubscribeDatapoint(String address) {
		// TODO Auto-generated method stub
		
	}
	
}
