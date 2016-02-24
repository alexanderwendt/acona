package at.tuwien.ict.kore.cell.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.cell.datastructures.Datapackage;
import at.tuwien.ict.kore.cell.datastructures.DatapackageImpl;

public class DataStorageImpl implements DataStorage {
	
	private static Logger log = LoggerFactory.getLogger(DataStorageImpl.class);

	private final Map<String, Datapackage> data = new ConcurrentHashMap<String, Datapackage>();
	private final Map<String, List<String>> subscribers = new ConcurrentHashMap<String, List<String>>();
	private DataStorageSubscriberNotificator subscriberNotificator;
	
	
	
	@Override
	public DataStorage init(DataStorageSubscriberNotificator subscriberManager) {
		this.subscriberNotificator = subscriberManager;
		return this;
		
	}

	@Override
	public void write(String address, Datapackage datapackage, String caller) {
		log.debug("write address={} data={}", address, datapackage);
		this.data.put(address, datapackage);
		this.notifySubscribers(address, datapackage, caller);
	}

	@Override
	public void add(String address, Datapackage datapackage, String caller) {
		throw new UnsupportedOperationException("Method not implemented yet");
		//this.data.merge(address, datapackage, Datapackage::);
		//this.data.mer
		
	}

	@Override
	public Datapackage read(String address) {
		Datapackage result = null;
		if (this.data.containsKey(address)) {
			result = this.data.get(address);
		} else {
			result = DatapackageImpl.newDatapackage();
		}
		
		return result;
	}
	
	private void notifySubscribers(String address, Datapackage datapackage, String caller) {
		//Get all subscribers for this datapoint if it exists
		final List<String> subscribers = new LinkedList<String>();	//Important to make if final as the caller is being removed. Else the source is modified
		if (this.subscribers.containsKey(address)==true) {
			subscribers.addAll(this.subscribers.get(address));
		}
		
		//Remove the caller from the subscibers to be notified. The system shall not notify itself
		subscribers.remove(caller);
		
		//Trigger subscriber behavior
		this.subscriberNotificator.notifySubscribers(subscribers, address, datapackage);
		
	}

	@Override
	public void subscribeDatapoint(String address, String caller) {
		//If the address exist and caller has not been added yet
		if (this.subscribers.containsKey(address)==true) {
			if (this.subscribers.get(address).contains(caller)==false) {
				this.subscribers.get(address).add(caller);	//Add caller
			}
		} else {
			//Add key
			List<String> subscriberlist = new LinkedList<String>();
			subscriberlist.add(caller);
			this.subscribers.put(address, subscriberlist);
		}
	}

	@Override
	public void unsubscribeDatapoint(String address, String caller) {
		//If address exist
		if (this.subscribers.containsKey(address)==true) {
			this.subscribers.get(address).remove(caller);
		}
		//else do nothing as there is no such subscriber
	}

	@Override
	public void remove(String address, String caller) {
		this.data.remove(address);
		this.notifySubscribers(address, DatapackageImpl.newDatapackage(), caller);
		
	}
	
}
