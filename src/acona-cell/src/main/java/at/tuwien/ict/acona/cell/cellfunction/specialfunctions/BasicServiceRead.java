package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class BasicServiceRead extends CellFunctionBasicService implements ReadDatapoint {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	//private static Logger log = LoggerFactory.getLogger(BasicServiceRead.class);

	//	public static final String READMETHOD = "read";
	//
	//	private static final String ACKNOWLEDGE = "OK";
	//	private static final String ERROR = "ERROR";
	//	private static final String PARAMETERRESULT = "result";
	//	private static final String PARAMETERSENDER = "sender";
	//	private static final String METHOD = "method";
	//	private static final String PARAMETERDATAPOINTS = "datapoints";

	//private String currentCaller = this.getCell().getLocalName();

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameter, String caller) {
		List<Datapoint> result = new ArrayList<>();
		try {
			//Set the current caller of the method
			//this.currentCaller = caller;

			// Convert parameter to datapoint
			//String sender = parameter.get(PARAMETERSENDER).getValueAsString();
			//String method = parameter.get(METHOD).getValueAsString();

			//JsonArray array = parameter.get(PARAMETER).getValue().getAsJsonArray();
			List<Datapoint> datapoints = Lists.newArrayList(parameter.values()); //GsonUtils.convertJsonArrayToDatapointList(array);
			// array.forEach(o -> {
			// datapoints.add(Datapoint.toDatapoint(o.getAsJsonObject()));
			// });

			//List<Datapoint> readDatapoints = new ArrayList<Datapoint>();

			//switch (method) {
			//case READMETHOD:
			result.addAll(this.read(datapoints));
			//	break;
			//default:
			//	throw new Exception("Method " + method + " not available.");
			//}

		} catch (Exception e) {
			log.error("Cannot perform operation", e);
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
		}

		// TODO Auto-generated method stub
		return result;
	}

	//	@Override
	//	protected void cellFunctionInit() throws Exception {
	//		// Generate external service in JADE
	//		this.getFunctionConfig().setGenerateReponder(true);
	//		// Use the request protocol
	//		this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	//
	//	}

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

	@Override
	public List<Datapoint> read(final List<Datapoint> datapointList) {
		List<Datapoint> result = new ArrayList<>();

		datapointList.forEach(dp -> {
			result.add(this.getCell().getDataStorage().read(dp.getAddress()));
		});

		return result;
	}

}
