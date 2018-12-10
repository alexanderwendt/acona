package at.tuwien.ict.acona.mq.cell.cellfunction.codelets;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.datastructures.Request;

/**
 * This is a codelet that executes codelet handlers.
 * 
 * @author wendt
 */
public class CellFunctionHandlerTriggerCodelet extends CellFunctionCodelet {

	private final static Logger log = LoggerFactory.getLogger(CellFunctionHandlerTriggerCodelet.class);

	public static final String codeletHandlerServiceUriName = "codelethandlernuri";

	//private String codeletHandlerAgent;
	private String codeletHandlerAddress;

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		String codeletHandlerUri = this.getFunctionConfig().getProperty(codeletHandlerServiceUriName);
		//codeletHandlerAgent = codeletHandlerUri.split(":")[0];
		codeletHandlerAddress = codeletHandlerUri;
	}

	@Override
	protected void executeFunction() throws Exception {
		log.info("=== Codelet handler={} running.===", codeletHandlerAddress);

		// run the codelet handler
		// this.getCommunicator().execute(codeletHandlerAgent, codeletHandlerAddress, Arrays.asList(
		// Datapoint.newDatapoint("method").setValue("executecodelethandler"),
		// Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(false))), 1000);

		Request req = new Request();

		this.getCommunicator().execute(codeletHandlerAddress + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, req); //executeServiceQueryDatapoints(codeletHandlerAgent, codeletHandlerAddress, req, codeletHandlerAgent, codeletHandlerAddress + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), this.getCommunicator().getDefaultTimeout());

		log.info("=== Codelet handler={} finished. ===", codeletHandlerAddress);
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
