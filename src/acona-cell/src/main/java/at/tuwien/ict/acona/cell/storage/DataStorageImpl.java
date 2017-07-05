package at.tuwien.ict.acona.cell.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils;
import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils.ConflictStrategy;

public class DataStorageImpl implements DataStorage {

	private static Logger log = LoggerFactory.getLogger(DataStorageImpl.class);

	private final Map<String, Datapoint> data = new ConcurrentHashMap<>();
	private final Map<String, List<String>> subscribers = new ConcurrentHashMap<>();
	private DataStorageSubscriberNotificator subscriberNotificator;

	@Override
	public DataStorage init(DataStorageSubscriberNotificator subscriberManager) {
		this.subscriberNotificator = subscriberManager;
		return this;

	}

	@Override
	public synchronized void write(Datapoint datapackage, String caller) throws Exception {
		// Get data
		// Datapackage previousDatapackage = this.read(address);
		// Only update subscribers if value has changed and one of them were
		// empty before
		// if (datapackage.isEmpty()==false && datapackage.isEmpty()==false) {

		if (datapackage.getAddress().contains("*") || datapackage.getAddress().contains(":")) {
			throw new Exception("* or : was part of the address: " + datapackage.getAddress() + "This is not allowed");
		}

		this.data.put(datapackage.getAddress(), Datapoints.newDatapoint(datapackage.getAddress()).setValue(datapackage.getValue()));
		log.debug("write datapoint={}", datapackage);
		this.notifySubscribers(datapackage, caller);
		// }
	}

	@Override
	public void append(Datapoint datapackage, String caller) throws Exception {
		if (datapackage.getAddress().contains("*") || datapackage.getAddress().contains(":")) {
			throw new Exception("* or : was part of the address: " + datapackage.getAddress() + "This is not allowed");
		}

		//Lock data
		synchronized (this.data) {
			GsonUtils util = new GsonUtils();
			Datapoint source = this.data.get(datapackage.getAddress());
			if (source != null) {
				if (source.getValue().isJsonObject() == false || datapackage.getValue().isJsonObject() == false) {
					throw new Exception(source + " is no json object or " + datapackage + " is no json object.");
				}

				util.extendJsonObject(source.getValue().getAsJsonObject(), ConflictStrategy.PREFER_SECOND_OBJ, datapackage.getValue().getAsJsonObject());

				//write appended message
				this.write(source, caller);

			} else {
				//write only new message
				this.write(datapackage, caller);
			}
		}

		//this.data.put(datapackage.getAddress(), datapackage);
		//log.debug("write datapoint={}", datapackage);
		//this.notifySubscribers(datapackage, caller);
		// }

	}

	@Override
	public void add(Datapoint datapackage, String caller) {
		throw new UnsupportedOperationException("Method not implemented yet");
		// this.data.merge(address, datapackage, Datapackage::);
		// this.data.mer

	}

	@Override
	public Datapoint readFirst(String address) {
		Datapoint result = null;
		List<Datapoint> list = this.read(address);

		if (list.isEmpty() == false) {
			result = list.get(0);
		} else {
			if (address.contains("*")) {
				result = Datapoints.newNullDatapoint();
			} else {
				result = Datapoints.newDatapoint(address);
			}

		}

		return result;
	}

	@Override
	public List<Datapoint> read(String address) {
		List<Datapoint> result = new ArrayList<>();

		if (address.endsWith("*")) {
			String startAddress = address.substring(0, address.length() - 1);
			result = this.data.entrySet()
					.stream()
					.filter(entry -> entry.getKey().startsWith(startAddress))
					.map(Map.Entry::getValue)
					.collect(Collectors.toList());
		} else {
			if (this.data.containsKey(address)) {
				result.add(this.data.get(address));
			}
		}

		if (result.isEmpty()) {
			result.add(Datapoints.newDatapoint(address));
		}

		return result;
	}

	private synchronized void notifySubscribers(Datapoint datapackage, String caller) {
		// Get all subscribers for this datapoint if it exists
		final List<String> subscribers = new LinkedList<>(); // Important
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
			List<String> subscriberlist = new LinkedList<>();
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
		this.notifySubscribers(Datapoints.newDatapoint(address), caller);

	}

	@Override
	public Map<String, List<String>> getSubscribers() {
		return Collections.unmodifiableMap(this.subscribers);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\nData storage content:\n");
		data.keySet().forEach(k -> {
			builder.append(k + " : " + data.get(k));
			builder.append("\n");
		});

		builder.append("Subscribers:\n");
		subscribers.keySet().forEach(k -> {
			builder.append(k + " : " + subscribers.get(k));
			builder.append("\n");
		});

		return builder.toString();
	}

}
