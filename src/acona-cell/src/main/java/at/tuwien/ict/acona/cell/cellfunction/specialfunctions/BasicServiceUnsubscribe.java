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
import jade.domain.FIPANames;

public class BasicServiceUnsubscribe extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	@Override
	public List<Datapoint> performOperation(final Map<String, Datapoint> parameter, String caller) {
		List<Datapoint> result = new ArrayList<>();
		try {
			// Convert parameter to datapoint
			//JsonArray array = parameter.get(PARAMETER).getValue().getAsJsonArray();
			List<Datapoint> datapoints = Lists.newArrayList(parameter.values());//GsonUtils.convertJsonArrayToDatapointList(array);

			this.unsubscribe(datapoints, caller);

			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));

		} catch (Exception e) {
			log.error("Cannot perform operation of parameter={}", parameter, e);
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
		}

		return result;
	}

	@Override
	protected void cellFunctionInit() throws Exception {
		// Generate external service in JADE
		this.getFunctionConfig().setGenerateReponder(true);
		// Use the request protocol
		this.getFunctionConfig().setResponderProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		log.debug("Function init: Set service={} to generate a responder with the protocol {}", this.getFunctionName(),
				this.getFunctionConfig().getResponderProtocol());

	}

	@Override
	protected void shutDownImplementation() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

	private void unsubscribe(final List<Datapoint> datapointList, String caller) {
		datapointList.forEach(dp -> {
			try {
				this.getCell().getDataStorage().unsubscribeDatapoint(dp.getAddress(), caller);
			} catch (Exception e) {
				log.error("Cannot unsubscribe datapoint={}", dp.getAddress(), e);
			}

		});
	}

}
