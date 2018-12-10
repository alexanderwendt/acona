package at.tuwien.ict.acona.mq.cell.cellfunction.helper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodelet;

public class IncrementOnConditionCodelet extends CellFunctionCodelet {

	private static Logger log = LoggerFactory.getLogger(IncrementOnConditionCodelet.class);

	public static final String attributeCheckAddress = "checkaddress";
	public static final String attributeConditionValue = "checkvalue";

	private String checkAddress = "";
	private int conditionValue = -1;

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		this.checkAddress = this.getFunctionConfig().getProperty(attributeCheckAddress);
		this.conditionValue = Integer.valueOf(this.getFunctionConfig().getProperty(attributeConditionValue));
		log.info("Codelet initialized");
	}

	@Override
	protected void executeFunction() throws Exception {
		// Read address
		int value = 0;
		String checkValue = this.getCommunicator().read(checkAddress).getValue().toString();
		log.debug("Read value={}", checkValue);
		if (checkValue.equals("{}") == false) {
			value = this.getCommunicator().read(checkAddress).getValue().getAsInt();
		}

		if (value == conditionValue) {
			log.info("Value={} matched. Increment it by 1", value);
			int newValue = value + 1;
			this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.checkAddress).setValue(new JsonPrimitive(newValue)));
		} else {
			log.info("Value={} does not match the condition value={}.", value, conditionValue);
		}

		log.info("Codelet finished");
	}

	@Override
	public void resetCodelet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
