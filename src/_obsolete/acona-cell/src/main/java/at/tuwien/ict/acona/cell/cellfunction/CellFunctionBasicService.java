package at.tuwien.ict.acona.cell.cellfunction;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.domain.FIPANames;

public abstract class CellFunctionBasicService extends CellFunctionImpl {

	private static Logger log = LoggerFactory.getLogger(CellFunctionBasicService.class);

	// public final String METHODNAME;

	// protected static final String ACKNOWLEDGEVALUE = "OK";
	// protected static final String ERRORVALUE = "ERROR";
	// protected static final String PARAMETERRESULTADDRESS = "result";
	// //private static final String PARAMETERSENDER = "sender";
	// //private static final String METHODPROPERTY = "method";
	// //protected static final String PARAMETER = "datapoints";

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	// public BasicService(String methodName) {
	// this.METHODNAME = methodName;
	// }

	@Override
	protected void cellFunctionInit() throws Exception {
		// Generate external service in JADE
		this.getFunctionConfig().setGenerateReponder(true);
		// Use the request protocol
		this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		this.basicServiceInit();

		log.debug("Function init: Set service={} to generate a responder with the protocol {}", this.getFunctionName(), this.getFunctionConfig().getResponderProtocol());
	}

	protected void basicServiceInit() throws Exception {

	}

	@Override
	protected void shutDownImplementation() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
