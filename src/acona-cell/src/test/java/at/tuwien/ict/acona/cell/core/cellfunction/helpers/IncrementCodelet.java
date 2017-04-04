package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class IncrementCodelet extends CellFunctionCodelet {

	private static Logger log = LoggerFactory.getLogger(IncrementCodelet.class);

	public static final String attributeCheckAddress = "checkaddress";
	public static final String attributeCheckValue = "checkvalue";

	private String checkAddress = "";
	private int conditionValue = -1;

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		this.checkAddress = this.getFunctionConfig().getProperty(attributeCheckAddress);
		this.conditionValue = Integer.valueOf(this.getFunctionConfig().getProperty(attributeCheckValue));
		log.info("Codelet initialized");
	}

	@Override
	protected void executeFunction() throws Exception {
		//Read address

		int value = 0;
		String checkValue = this.getCommunicator().read(checkAddress).getValue().toString();
		if (checkValue.equals("{}") == false) {
			value = this.getCommunicator().read(checkAddress).getValue().getAsInt();
		}

		if (value == conditionValue) {
			log.info("Value={} matched. Increment it by 1", value);
			int newValue = value + 1;
			this.getCommunicator().write(Datapoint.newDatapoint(this.checkAddress).setValue(new JsonPrimitive(newValue)));
		} else {
			log.info("Value={} does not match the condition value={}.", value, conditionValue);
		}

		log.info("Codelet finished");
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
