package at.tuwien.ict.acona.cell.cellfunction.special;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ActivatorConditionManager {
	
	private static Logger log = LoggerFactory.getLogger(ActivatorConditionManager.class);
	
	private final Condition condition;
	private Datapoint currentValue = null;
	private Datapoint previousValue = null;
	private final String datapointAddress;
	private boolean isConditionFulfilled;
	
	public ActivatorConditionManager(Condition condition, String datapointAddress) {
		this.condition = condition;
		this.datapointAddress = datapointAddress;
	}

	public Datapoint getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Datapoint currentValue) {
		this.currentValue = currentValue;
	}

	public Datapoint getPreviousValue() {
		return previousValue;
	}

	public void setPreviousValue(Datapoint previousValue) {
		this.previousValue = previousValue;
	}
	
	public boolean testCondition(Datapoint value) {
		 boolean result=false;
		 try {
			 result = this.condition.testCondition(value);
		 } catch (Exception e) {
		     log.error("Condition cannot be executed. return false as result", e);
		 }
		 
		 this.previousValue = this.currentValue;
		 
		 //If condition is fulfilled, update the value with the new incoming value, else set current value to null.
		 if (result==true) {
			 this.currentValue = value;
			 this.isConditionFulfilled = true;
		 } else {
			 this.currentValue = null;
			 this.isConditionFulfilled = false;
		 }
		 
		 return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("condition=");
		builder.append(condition);
		builder.append(", datapointAddress=");
		builder.append(datapointAddress);
		builder.append(", currentVal=");
		builder.append((currentValue!=null?currentValue.getValue():"null"));
		builder.append(", previousVal=");
		builder.append((previousValue!=null?previousValue.getValue():null));
		return builder.toString();
	}

	public boolean isConditionFulfilled() {
		return this.isConditionFulfilled;
	}
	
	public String getName() {
		return this.condition.getName();
	}
	
	
}
