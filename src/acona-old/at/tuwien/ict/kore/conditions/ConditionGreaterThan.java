package at.tuwien.ict.kore.conditions;

import _OLD_at.tuwien.ict.acona.cell.cellfunction.special.ConditionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionGreaterThan extends ConditionImpl {

	private static final String REFVALUEADDRESS = "referencevalue";
	private double referenceValue = 0;
	
	@Override
	protected void subInit() {
		this.referenceValue = this.conf.get(REFVALUEADDRESS).getAsDouble();
		log.debug("{}>Reference value = {}", this.name, this.referenceValue);
		
	}

	@Override
	public boolean testCondition(Datapoint data) {
		boolean result = false;
		
		//Check if !=null and if it is a double
		if (data.getValue()!=null && data.getValue().isJsonPrimitive() && data.getValue().getAsDouble()>this.referenceValue) {
			result = true;
		}
		
		return result;
	}

	@Override
	public String getDescription() {
		return "Tests if a value is !=null and greater than a reference value";
	}

}
