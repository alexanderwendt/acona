package at.tuwien.ict.acona.mq.core.agentfunction.helper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletImpl;

public class IncrementNumberCodelet extends CodeletImpl {

	private static final Logger log = LoggerFactory.getLogger(IncrementNumberCodelet.class);

	public static final String ATTRIBUTESERVICENAME = "servicename";
	public static final String ATTRIBUTESUBADDRESS = "subaddress";
	private String serviceName = "";
	private String subAddress = "incrementme";

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		serviceName = this.getFunctionConfig().getProperty(ATTRIBUTESERVICENAME, serviceName);
		subAddress = this.getFunctionConfig().getProperty(ATTRIBUTESUBADDRESS, subAddress);
		log.info("IncrementCodelet initialized with servicename = {} and subaddress= {}", this.serviceName, "." + subAddress);
	}

	@Override
	protected void executeFunction() throws Exception {
		log.debug("Start increment service codelet");
		// this.getCommunicator().executeServiceQueryDatapoints(this.getCell().getLocalName(), serviceName, new ArrayList<Datapoint>(), this.getCell().getLocalName(), serviceName + ".state", 1000);
		// String commandDatapoint = serviceName + ".command";
		// String resultDatapoint = serviceName + ".state";
		// Datapoint result1 = this.getCommunicator().queryDatapoints(commandDatapoint, new JsonPrimitive(ControlCommand.START.toString()), this.getCell().getLocalName(), resultDatapoint,
		// this.getCell().getLocalName(), 10000);
		int value = this.getCommunicator().read(this.getWorkingMemoryAddress() + "." + subAddress).getValue().getAsInt(); // this.getWorkingMemoryAddress()
		log.debug("Read value={} from working memory", value);
		// log.debug("Increment");
		int value2 = value + 1;
		this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.getWorkingMemoryAddress() + "." + subAddress).setValue(value2));
		log.info("Value={} incremented to {} and written to working memory", value, value2);
	}

	@Override
	public void resetCodelet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDownCodelet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
