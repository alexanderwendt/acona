package at.tuwien.ict.acona.cell.activator.conditions;

import at.tuwien.ict.acona.cell.activator.ConditionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionIsNotEmpty extends ConditionImpl {

	@Override
	public boolean testCondition(Datapoint data) {
		boolean result = false;
		
		if (data.getValue().isJsonNull()==false) {
			result = true;
		}
		
		log.debug("Condition gives={}", result);
		
		return result;
	}

}
