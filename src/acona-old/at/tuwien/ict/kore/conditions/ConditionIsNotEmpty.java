package at.tuwien.ict.kore.conditions;

import _OLD_at.tuwien.ict.acona.cell.cellfunction.special.ConditionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionIsNotEmpty extends ConditionImpl {

	private String test = "";
	
	@Override
	public boolean testCondition(Datapoint data) {
		boolean result = false;
		
		
		if (data.getValue().isJsonNull()==false && data.getValue().getAsString().equals("")==false) {
			result = true;
		}
		
		log.trace("{}>result={}", this.name, result);
		
		return result;
	}

	@Override
	public String getDescription() {
		return "Check if a datapoint is empty";
	}

	@Override
	protected void subInit() {
		//test = (this.conf!=null?"":this.conf.get("TEST").getAsString());
		
	}

}
