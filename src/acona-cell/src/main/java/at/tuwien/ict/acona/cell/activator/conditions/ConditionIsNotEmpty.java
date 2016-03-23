package at.tuwien.ict.acona.cell.activator.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.datastructures.Datapackage;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionIsNotEmpty implements Condition {
	
	protected static Logger log = LoggerFactory.getLogger(ConditionIsNotEmpty.class);

	@Override
	public boolean testCondition(Datapoint data) {
		boolean result = false;
		
		log.debug("Condition gives={}", result);
		
		return result;
	}

	@Override
	public void init(String name, JsonObject settings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getName() {
		// TODO Auto-generated method stub
		
	}

}
