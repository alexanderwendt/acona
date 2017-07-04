package at.tuwien.ict.acona.cell.core.cellfunction.codelets.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;

public class IncrementNumberCodelet extends CellFunctionCodelet {

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
		//this.getCommunicator().executeServiceQueryDatapoints(this.getCell().getLocalName(), serviceName, new ArrayList<Datapoint>(), this.getCell().getLocalName(), serviceName + ".state", 1000);
		//String commandDatapoint = serviceName + ".command";
		//String resultDatapoint = serviceName + ".state";
		//Datapoint result1 = this.getCommunicator().queryDatapoints(commandDatapoint, new JsonPrimitive(ControlCommand.START.toString()), this.getCell().getLocalName(), resultDatapoint, this.getCell().getLocalName(), 10000);
		int value = this.getCommunicator().read(this.getWorkingMemoryAddress() + "." + subAddress).getValue().getAsInt(); //this.getWorkingMemoryAddress()
		log.debug("Read value={} from working memory");
		log.debug("Increment");
		value++;
		this.getCommunicator().write(Datapoints.newDatapoint(this.getWorkingMemoryAddress() + "." + subAddress).setValue(value));
		log.debug("Value={} written to working memory", value);
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
