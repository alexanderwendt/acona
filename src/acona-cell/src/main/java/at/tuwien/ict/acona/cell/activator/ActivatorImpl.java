package at.tuwien.ict.acona.cell.activator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.core.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ActivatorImpl implements Activator {

	protected static Logger log = LoggerFactory.getLogger(ActivatorImpl.class);
	
	private String name;
	private CellFunctionBehaviour behavior;
	private Cell caller;
	//Mapping of one condition to a datapoint. N X M mapping possible. Datapoint to m Conditions
	private final Map<String, List<ActivatorConditionManager>> conditionMapping = new ConcurrentHashMap<String, List<ActivatorConditionManager>>();
	
	//FIXME temporary solution, where the number of filfilled conditions it count. If all conditions are fulfilled, the currentCount=Maxcount
	//All conditions must be fulfilled for the behavior to be activated
	private int conditionMaxCount=0;
	private volatile int conditionCurrentCount = 0;

	@Override
	public Activator init(String name, Map<String, List<Condition>> subscriptionCondition, String logic, CellFunctionBehaviour behavior, Cell caller) {
		this.name = name;
		this.behavior = behavior;
		this.caller = caller;
			
		//Set conditions and 
		subscriptionCondition.entrySet().forEach((Entry<String, List<Condition>> e)->{
			
			//Create an activator condition
			List<ActivatorConditionManager> conditionManagerList = new LinkedList<ActivatorConditionManager>();
			e.getValue().forEach((Condition c)->{
				//Create new condition manager from mapping
				ActivatorConditionManager conditionManager = new ActivatorConditionManager(c, e.getKey());
				conditionManagerList.add(conditionManager);
			});
			
			conditionMapping.put(e.getKey(), conditionManagerList);
			
			//Subscribe datapoint
			this.caller.getDataStorage().subscribeDatapoint(e.getKey(), caller.getName());
			
		});
		
		//Count table
		this.conditionMaxCount = calculateTotalCount(subscriptionCondition);
		
		//Add and activate behavior
		//INFO: Instead of letting the caller add the jade behavior, the cell behavior adds itself
		behavior.addBehaviourToCallerCell(caller);
		
		
		//Read the initial value from the datapoint to trigger the conditions, especially the always true conditions
		log.trace("{}>Initialize all conditions, by testing them with their subscribed data", this.name);
		subscriptionCondition.entrySet().forEach((Entry<String, List<Condition>> e)->{
			this.runActivation(this.caller.getDataStorage().read(e.getKey()));
		});
		
		
		log.trace("{}> initialization finished", this.name);

		return this;
	}

	/**
	 * Calculate the total count for elements in a map with a list within
	 * 
	 * @param subscriptionCondition
	 * @return
	 */
	private int calculateTotalCount(Map<String, List<Condition>> subscriptionCondition) {
		int conditionMaxCount=0;
		for (Entry<String, List<Condition>> entry: subscriptionCondition.entrySet()) {
			conditionMaxCount += entry.getValue().size();	//Values of conditions in subtables
		}
		
		return conditionMaxCount;
	}
	
	@Override
	public void closeActivator() {
		this.conditionMapping.forEach((e, v)->{
			this.caller.getDataStorage().unsubscribeDatapoint(e, this.caller.getName());
		});
		
	}

	@Override
	public boolean runActivation(Datapoint subscribedData) {
		//This method is activated as soon as conditions shall be tested
		List<ActivatorConditionManager> conditions;
		if (this.conditionMapping.containsKey(subscribedData.getAddress())==true) {
			conditions = this.conditionMapping.get(subscribedData.getAddress());
		} else {
			conditions = new LinkedList<ActivatorConditionManager>(); 
		}
			
		//Check all conditions
		//log.trace("{}> on data={}", this.name, subscribedData);
		//boolean isActivate=true;
		
		//Run all conditions for this datapoint (ActivatorConditionManager c {do c....}
		conditions.forEach(c->{
			boolean previousState = c.isConditionFulfilled();
			boolean currentState = c.testCondition(subscribedData);
			log.trace("{}>Test condition={}. Result={}", this.name, c, currentState);
			
			//If state changed from false to true, increment. In the other direction, decrement
			if (currentState==true && previousState==false) {
				this.conditionCurrentCount++;
			} else if (currentState==false && previousState==true) {
				this.conditionCurrentCount--;
			}
		});
		
		//Check if all conditions are fulfilled
		boolean result = false;
		if (this.conditionCurrentCount==this.conditionMaxCount) {
			result = true;
			
			//Extract all stored datapoints from the table
			//Create datapointlist
			final Map<String, Datapoint> currentDatapointList = new ConcurrentHashMap<String, Datapoint>();
			//For each entry in the condition mapping, get all datapoints from the first element in the activation manager list 
			conditionMapping.forEach((k, v)->currentDatapointList.put(k, v.get(0).getCurrentValue()));
			
			behavior.setData(currentDatapointList);	//Add data that shall be used in the behavior
			behavior.setRunPermission(true);	//Set permission true to start
			behavior.startBehaviour();	//Start behavior
		} else {
			log.trace("{}> No activation. Condition(s) did not match.", this.name);
		}
		
		return result;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);
		builder.append(", behavior=");
		builder.append(behavior.getName());
		builder.append(", caller=");
		builder.append(caller.getLocalName());
		builder.append(", conditions=");
		builder.append(conditionMapping);
		return builder.toString();
	}

	@Override
	public List<String> getLinkedDatapoints() {
		return Collections.unmodifiableList(new ArrayList<String>(this.conditionMapping.keySet()));
	}

	@Override
	public Map<String, List<ActivatorConditionManager>> getConditionMapping() {
		return this.conditionMapping;
	}

}
