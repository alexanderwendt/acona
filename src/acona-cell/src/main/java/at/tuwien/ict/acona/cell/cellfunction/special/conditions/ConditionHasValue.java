package at.tuwien.ict.acona.cell.cellfunction.special.conditions;

import at.tuwien.ict.acona.cell.cellfunction.special.ConditionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionHasValue extends ConditionImpl {

	private static final String COMAPRESTRING = "comparestring";
	private String expectedString = "";
	
	@Override
	public boolean testCondition(Datapoint data) {
		boolean result = false;
		
		
		if (data.getValue().isJsonNull()==false && data.getValue().getAsString().equals(expectedString)==true) {
			result = true;
		}
		
		log.trace("{}>value comparison: expected value={}, actual value={}, result={}", this.name, expectedString, data.getValue(), result);
		
		return result;
	}

	@Override
	public String getDescription() {
		return "Check if a datapoint has a certain value as a string";
	}

	@Override
	protected void subInit() throws Exception {
		if (this.conf.get(COMAPRESTRING)!=null) {
			expectedString = this.conf.get(COMAPRESTRING).getAsString();
		} else {
			throw new Exception("Required parameter " + COMAPRESTRING + " does not exist in the config");
		}
		
		
	}

}
