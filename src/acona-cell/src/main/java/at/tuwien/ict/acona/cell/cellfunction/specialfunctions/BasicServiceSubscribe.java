package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.domain.FIPANames;

public class BasicServiceSubscribe extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	//	private static Logger log = LoggerFactory.getLogger(BasicServiceSubscribe.class);
	//
	//	public static final String METHODNAME = "subscribe";
	//
	//	private static final String ACKNOWLEDGE = "OK";
	//	private static final String ERROR = "ERROR";
	//	private static final String PARAMETERRESULT = "result";
	//	private static final String PARAMETERSENDER = "sender";
	//	private static final String METHODPROPERTY = "method";
	//	private static final String PARAMETERDATAPOINTS = "datapoints";

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	@Override
	public List<Datapoint> performOperation(final Map<String, Datapoint> parameter, String caller) {
		List<Datapoint> result = new ArrayList<Datapoint>();
		try {
			// Convert parameter to datapoint
			//JsonArray array = parameter.get(PARAMETER).getValue().getAsJsonArray();
			List<Datapoint> datapoints = Lists.newArrayList(parameter.values());//GsonUtils.convertJsonArrayToDatapointList(array);

			result.addAll(this.subscribe(datapoints, caller));
			log.debug("Agent {} subscribed {}", caller, result);
		} catch (Exception e) {
			log.error("Cannot perform operation of parameter={}", parameter, e);
			result.add(Datapoint.newDatapoint(PARAMETERRESULTADDRESS).setValue(ERRORVALUE));
		}

		return result;
	}

	@Override
	protected void cellFunctionInit() throws Exception {
		// Generate external service in JADE
		this.getFunctionConfig().setGenerateReponder(true);
		// Use the request protocol
		this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);

		log.debug("Function init: Set service={} to generate a responder with the protocol {}", this.getFunctionName(),
				this.getFunctionConfig().getResponderProtocol());

	}

	private List<Datapoint> subscribe(final List<Datapoint> datapointNameList, String caller) {
		List<Datapoint> result = new ArrayList<Datapoint>();
		datapointNameList.forEach(dp -> {
			try {
				this.getCell().getDataStorage().subscribeDatapoint(dp.getAddress(), caller);
				result.add(this.getCell().getDataStorage().read(dp.getAddress()));
			} catch (Exception e) {
				log.error("Cannot subscribe datapoint={}", dp.getAddress(), e);
			}
		});

		return result;
	}

}
