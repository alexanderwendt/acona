package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import jade.domain.FIPANames;

/**
 * @author wendt
 * 
 *         Unsubscribe data from the data storage.
 *
 */
public class BasicServiceUnsubscribe extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	@Override
	public JsonRpcResponse performOperation(final JsonRpcRequest parameter, String caller) {
		JsonRpcResponse result = null;
		try {

			List<String> addresses = parameter.getParameter(0, new TypeToken<List<String>>() {});
			this.unsubscribe(addresses, caller);

			result = new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));
		} catch (Exception e) {
			log.error("Cannot perform operation of parameter={}", parameter, e);
			result = new JsonRpcResponse(parameter, new JsonRpcError("UnsubscribeError", -1, e.getMessage(), e.getLocalizedMessage()));
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

	private void unsubscribe(final List<String> datapointList, String caller) {
		datapointList.forEach(dp -> {
			try {
				this.getCell().getDataStorage().unsubscribeDatapoint(dp, caller);
			} catch (Exception e) {
				log.error("Cannot unsubscribe datapoint={}", dp, e);
			}

		});
	}

}
