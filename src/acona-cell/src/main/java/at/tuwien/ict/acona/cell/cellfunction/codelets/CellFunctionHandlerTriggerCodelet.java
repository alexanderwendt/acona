package at.tuwien.ict.acona.cell.cellfunction.codelets;

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

/**
 * This is a codelet that executes codelet handlers.
 * 
 * @author wendt
 *
 */
public class CellFunctionHandlerTriggerCodelet extends CellFunctionCodelet {

	private final static Logger log = LoggerFactory.getLogger(CellFunctionHandlerTriggerCodelet.class);

	public static final String codeletHandlerServiceUriName = "codelethandlernuri";

	private String codeletHandlerAgent;
	private String codeletHandlerAddress;

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		String codeletHandlerUri = this.getFunctionConfig().getProperty(codeletHandlerServiceUriName);
		codeletHandlerAgent = codeletHandlerUri.split(":")[0];
		codeletHandlerAddress = codeletHandlerUri.split(":")[1];
	}

	@Override
	protected void executeFunction() throws Exception {
		log.info("Run codelet trigger for codelet handler={}", this.codeletHandlerAgent + ":" + this.codeletHandlerAddress);

		//run the codelet handler
		//		this.getCommunicator().execute(codeletHandlerAgent, codeletHandlerAddress, Arrays.asList(
		//				Datapoint.newDatapoint("method").setValue("executecodelethandler"),
		//				Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(false))), 1000);

		this.getCommunicator().executeServiceQueryDatapoints(codeletHandlerAgent, codeletHandlerAddress, Arrays.asList(
				Datapoint.newDatapoint("method").setValue("executecodelethandler"),
				Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(false))),
				codeletHandlerAgent, codeletHandlerAddress + ".result", 20000);

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		//No need to be executed as no datapoints are used. 

	}

}
