package at.tuwien.ict.acona.cell.core.cellfunction.codelets.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class IncrementServiceCodelet extends CellFunctionCodelet {

	private static final Logger log = LoggerFactory.getLogger(IncrementServiceCodelet.class);

	public static final String ATTRIBUTESERVICENAME = "servicename";
	private String serviceName = "";

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		serviceName = this.getFunctionConfig().getProperty(ATTRIBUTESERVICENAME, serviceName);
		log.info("IncrementCodelet initialized with servicename = {}", this.serviceName);
	}

	@Override
	protected void executeFunction() throws Exception {
		log.debug("Start increment service codelet");
		//this.getCommunicator().executeServiceQueryDatapoints(this.getCell().getLocalName(), serviceName, new ArrayList<Datapoint>(), this.getCell().getLocalName(), serviceName + ".state", 1000);
		//String commandDatapoint = serviceName + ".command";
		//String resultDatapoint = serviceName + ".state";
		//Datapoint result1 = this.getCommunicator().queryDatapoints(commandDatapoint, new JsonPrimitive(ControlCommand.START.toString()), this.getCell().getLocalName(), resultDatapoint, this.getCell().getLocalName(), 10000);
		int value = this.getCommunicator().read(this.getWorkingMemoryAddress() + ".incrementme").getValue().getAsInt(); //this.getWorkingMemoryAddress()
		log.debug("Read value={} from working memory");
		log.debug("Increment");
		value++;
		this.getCommunicator().write(Datapoint.newDatapoint(this.getWorkingMemoryAddress() + ".incrementme").setValue(value));
		log.debug("Value={} written to working memory", value);
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
