package OLD_at.tuwien.ict.acona.cell.activator.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _OLD_at.tuwien.ict.acona.cell.cellfunction.special.ConditionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ConditionAlwaysFalse extends ConditionImpl {
	private static Logger log = LoggerFactory.getLogger(ConditionAlwaysFalse.class);

	@Override
	protected void subInit() {
		if (this.conf!=null && this.conf.has("option1")==true && this.conf.has("option2")==true) {
			log.info("Got info from config: option1={}, option2={}", this.conf.getAsJsonPrimitive("option1").getAsString(), this.conf.getAsJsonPrimitive("option2").getAsString());
		} else {
			log.info("No configuration was passed");
		}
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
