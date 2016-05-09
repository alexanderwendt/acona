package at.tuwien.ict.acona.cell.activator.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.activator.ConditionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionAlwaysFalse extends ConditionImpl {
	private static Logger log = LoggerFactory.getLogger(ConditionAlwaysFalse.class);

	@Override
	protected void subInit() {
		//Nothing to init	
	}

	@Override
	public boolean testCondition(Datapoint data) {
		return false;
	}

	@Override
	public String getDescription() {
		return "Dummy condition";
	}

}
