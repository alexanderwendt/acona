package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.domain.FIPANames;

public abstract class BasicService extends CellFunctionImpl {

	private static Logger log = LoggerFactory.getLogger(BasicService.class);

	//public final String METHODNAME;

	protected static final String ACKNOWLEDGE = "OK";
	protected static final String ERROR = "ERROR";
	protected static final String PARAMETERRESULT = "result";
	//private static final String PARAMETERSENDER = "sender";
	//private static final String METHODPROPERTY = "method";
	//protected static final String PARAMETER = "datapoints";

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	//	public BasicService(String methodName) {
	//		this.METHODNAME = methodName;
	//	}

	@Override
	protected void cellFunctionInit() throws Exception {
		// Generate external service in JADE
		this.getFunctionConfig().setGenerateReponder(true);
		// Use the request protocol
		this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		log.debug("Function init: Set service={} to generate a responder with the protocol {}", this.getFunctionName(), this.getFunctionConfig().getResponderProtocol());
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
