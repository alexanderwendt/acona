package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class BasicServiceWrite extends BasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	//	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);
	//
	//	public static final String WRITEMETHOD = "write";
	//
	//	private static final String ACKNOWLEDGE = "OK";
	//	private static final String ERROR = "ERROR";
	//	private static final String PARAMETERRESULT = "result";
	//	//private static final String PARAMETERSENDER = "sender";
	//	private static final String METHOD = "method";
	//	private static final String PARAMETERDATAPOINTS = "datapoints";

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	@Override
	public List<Datapoint> performOperation(final Map<String, Datapoint> parameter, String caller) {
		List<Datapoint> result = new ArrayList<Datapoint>();
		try {
			// Convert parameter to datapoint
			//String sender = parameter.get(PARAMETERSENDER).getValueAsString();
			//String method = parameter.get(METHOD).getValueAsString();

			//JsonArray array = parameter.get(PARAMETER).getValue().getAsJsonArray();
			List<Datapoint> datapoints = Lists.newArrayList(parameter.values());//GsonUtils.convertJsonArrayToDatapointList(array);

			//switch (method) {
			//case WRITEMETHOD:
			this.write(datapoints, caller);
			//	break;
			//default:
			//	throw new Exception("Method " + method + " not available.");
			//}

			result.add(Datapoint.newDatapoint(PARAMETERRESULT).setValue(ACKNOWLEDGE));

		} catch (Exception e) {
			log.error("Cannot perform operation of parameter={}", parameter, e);
			result.add(Datapoint.newDatapoint(PARAMETERRESULT).setValue(ERROR));
		}

		return result;
	}

	//	@Override
	//	protected void cellFunctionInit() throws Exception {
	//		// Generate external service in JADE
	//		this.getFunctionConfig().setGenerateReponder(true);
	//		// Use the request protocol
	//		this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	//
	//		log.debug("Function init: Set service={} to generate a responder with the protocol {}", this.getFunctionName(),
	//				this.getFunctionConfig().getResponderProtocol());
	//
	//	}
	//
	//	@Override
	//	protected void shutDownImplementation() {
	//		// TODO Auto-generated method stub
	//
	//	}
	//
	//	@Override
	//	protected void updateDatapointsById(Map<String, Datapoint> data) {
	//		// TODO Auto-generated method stub
	//
	//	}

	private void write(final List<Datapoint> datapointList, String caller) {
		datapointList.forEach(dp -> {
			this.getCell().getDataStorage().write(dp, caller);
		});
	}

}
