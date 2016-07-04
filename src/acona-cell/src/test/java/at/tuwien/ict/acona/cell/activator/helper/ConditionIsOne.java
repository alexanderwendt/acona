package at.tuwien.ict.acona.cell.activator.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.activator.ConditionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionIsOne extends ConditionImpl {
	private static Logger log = LoggerFactory.getLogger(ConditionIsOne.class);

	@Override
	protected void subInit() {
		if (this.conf!=null && this.conf.has("option1")==true && this.conf.has("option2")==true) {
			log.info("Got info from config: option1={}, option2={}", this.conf.getAsJsonPrimitive("option1").getAsString(), this.conf.getAsJsonPrimitive("option2").getAsString());
		} else {
			log.info("No configuration was passed");
		}
	}

	@Override
	public boolean testCondition(Datapoint data) throws Exception {
		boolean result = false;
		
		try {
			if (data.getValue().getAsString().isEmpty()==false && data.getValue().getAsDouble()==1) {
				result = true;
			}
		} catch (Exception e) {
			log.error("Cannot execute condition. It should be programmed not to throw exceptions", e);
		}

		
		return result;
	}

	@Override
	public String getDescription() {
		return "Condition to return true if number is 1.0";
	}

}
