package at.tuwien.ict.acona.cell.storage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class DataStorageImpl implements DataStorage {

	private static Logger log = LoggerFactory.getLogger(DataStorageImpl.class);

	private final Map<String, Datapoint> data = new ConcurrentHashMap<String, Datapoint>();
	private final Map<String, List<String>> subscribers = new ConcurrentHashMap<String, List<String>>();
	private DataStorageSubscriberNotificator subscriberNotificator;

	@Override
	public DataStorage init(DataStorageSubscriberNotificator subscriberManager) {
		this.subscriberNotificator = subscriberManager;
		return this;

	}

	@Override
	public synchronized void write(Datapoint datapackage, String caller) {
		// Get data
		// Datapackage previousDatapackage = this.read(address);
		// Only update subscribers if value has changed and one of them were
		// empty before
		// if (datapackage.isEmpty()==false && datapackage.isEmpty()==false) {

		this.data.put(datapackage.getAddress(), datapackage);
		log.debug("write datapoint={}", datapackage);
		this.notifySubscribers(datapackage, caller);
		// }
	}

	@Override
	public void add(Datapoint datapackage, String caller) {
		throw new UnsupportedOperationException("Method not implemented yet");
		// this.data.merge(address, datapackage, Datapackage::);
		// this.data.mer

	}

	@Override
	public Datapoint read(String address) {
		Datapoint result = null;
		if (this.data.containsKey(address)) {
			result = this.data.get(address);
		} else {
			result = Datapoint.newDatapoint(address);
		}

		return result;
	}

	private synchronized void notifySubscribers(Datapoint datapackage, String caller) {
		// Get all subscribers for this datapoint if it exists
		final List<String> subscribers = new LinkedList<String>(); // Important
																	// to make
																	// if final
																	// as the
																	// caller is
																	// being
																	// removed.
																	// Else the
																	// source is
																	// modified
		if (this.subscribers.containsKey(datapackage.getAddress()) == true) {
			subscribers.addAll(this.subscribers.get(datapackage.getAddress()));
		}

		// Trigger subscriber behavior
		if (subscribers.isEmpty() == false && datapackage.getAddress().equals("") == false && datapackage != null) {
			this.subscriberNotificator.notifySubscribers(subscribers, caller, datapackage);
		}
	}

	@Override
	public synchronized void subscribeDatapoint(String address, String caller) {
		// If the address exist and caller has not been added yet
		if (this.subscribers.containsKey(address) == true) {
			if (this.subscribers.get(address).contains(caller) == false) {
				this.subscribers.get(address).add(caller); // Add caller
			}
		} else {
			// Add key
			List<String> subscriberlist = new LinkedList<String>();
			subscriberlist.add(caller);
			this.subscribers.put(address, subscriberlist);
		}
	}

	@Override
	public synchronized void unsubscribeDatapoint(String address, String caller) {
		// If address exist
		if (this.subscribers.containsKey(address) == true) {
			this.subscribers.get(address).remove(caller);
		}
		// else do nothing as there is no such subscriber
	}

	@Override
	public void remove(String address, String caller) {
		this.data.remove(address);
		this.notifySubscribers(Datapoint.newDatapoint(address), caller);

	}

	@Override
	public Map<String, List<String>> getSubscribers() {
		return Collections.unmodifiableMap(this.subscribers);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("data=");
		builder.append(data);
		builder.append(", subscribers=");
		builder.append(subscribers);
		return builder.toString();
	}

}
