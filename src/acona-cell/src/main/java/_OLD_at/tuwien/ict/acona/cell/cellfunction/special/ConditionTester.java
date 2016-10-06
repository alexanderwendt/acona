package at.tuwien.ict.acona.cell.cellfunction.special;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import _OLD.at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

/**
 * This class will act as a proxy for all cellfunction types, with or without conditions
 * 
 * @author wendt
 *
 */
public class ConditionTester {

	protected static Logger log = LoggerFactory.getLogger(ConditionTester.class);
	
	private String name;
	private Cell caller;
	//Mapping of one condition to a datapoint. N X M mapping possible. Datapoint to m Conditions
	private final Map<String, List<ActivatorConditionManager>> subscriptionConditionMapping = new ConcurrentHashMap<String, List<ActivatorConditionManager>>();	//Datapoint, condition
	//Map internal variables with datapoints
	private final Map<String, String> subscriptions = new HashMap<String, String>();	//Variable, datapoint
	
	//FIXME temporary solution, where the number of filfilled conditions it count. If all conditions are fulfilled, the currentCount=Maxcount
	//All conditions must be fulfilled for the behavior to be activated
	private int conditionMaxCount=0;
	private volatile int conditionCurrentCount = 0;
	
	public Activator init(String name, Map<String, List<Condition>> subscriptionCondition, JsonObject settings, Cell caller) throws Exception {
		this.name = name;
		this.caller = caller;
		
		//Add subscription from children to the list if there are any
		
		
		
		//Set conditions and 
		subscriptionCondition.entrySet().forEach((Entry<String, List<Condition>> e)->{
			
			//Create an activator condition
			List<ActivatorConditionManager> conditionManagerList = new LinkedList<ActivatorConditionManager>();
			e.getValue().forEach((Condition c)->{
				//Create new condition manager from mapping
				ActivatorConditionManager conditionManager = new ActivatorConditionManager(c, e.getKey());
				conditionManagerList.add(conditionManager);
			});
			
			subscriptionConditionMapping.put(e.getKey(), conditionManagerList);
			
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
	
	public void closeActivator() {
		this.subscriptionConditionMapping.forEach((e, v)->{
			this.caller.getDataStorage().unsubscribeDatapoint(e, this.caller.getName());
		});
		
	}

	public boolean runActivation(Datapoint subscribedData) {
		//This method is activated as soon as conditions shall be tested
		List<ActivatorConditionManager> conditions;
		if (this.subscriptionConditionMapping.containsKey(subscribedData.getAddress())==true) {
			conditions = this.subscriptionConditionMapping.get(subscribedData.getAddress());
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
			subscriptionConditionMapping.forEach((k, v)->currentDatapointList.put(k, v.get(0).getCurrentValue()));
			
			function.updateData(currentDatapointList);	//Add data that shall be used in the behavior
			//behavior.setRunPermission(true);	//Set permission true to start -- already done in updatedata
			//behavior.startBehaviour();	//Start behavior -- Behaviour is started within itself on update data
		} else {
			log.trace("{}> No activation. Condition(s) did not match.", this.name);
		}
		
		return result;
	}

	public String getActivatorName() {
		return this.name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);
		builder.append(", behavior=");
		builder.append(function.getFunctionName());
		builder.append(", caller=");
		builder.append(caller.getLocalName());
		builder.append(", conditions=");
		builder.append(subscriptionConditionMapping);
		return builder.toString();
	}

	public List<String> getSubscribedDatapoints() {
		return Collections.unmodifiableList(new ArrayList<String>(this.subscriptionConditionMapping.keySet()));
	}

	@Override
	public Map<String, List<ActivatorConditionManager>> getConditionMapping() {
		return this.subscriptionConditionMapping;
	}

	public Activator initCellFunctions(String name, Map<String, String> subscriptionMapping, Cell caller) {
		throw new UnsupportedOperationException();
	}

}
