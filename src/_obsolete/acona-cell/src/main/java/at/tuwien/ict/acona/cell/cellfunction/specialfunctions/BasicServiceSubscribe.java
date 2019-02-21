package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import jade.domain.FIPANames;

/**
 * 
 * 
 * @author wendt
 *
 *         Subscribe data from the data storage.
 *
 */
public class BasicServiceSubscribe extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameter, String caller) {
		JsonRpcResponse result = null;
		try {

			List<String> addresses = parameter.getParameter(0, new TypeToken<List<String>>() {});
			List<Datapoint> readValues = this.subscribe(addresses, caller);

			result = new JsonRpcResponse(parameter, readValues);

			log.debug("Agent {} subscribed {}", caller, result);
		} catch (Exception e) {
			log.error("Cannot perform operation of parameter={}", parameter, e);
			result = new JsonRpcResponse(parameter, new JsonRpcError("SubscribeError", -1, e.getMessage(), e.getLocalizedMessage()));
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

	private List<Datapoint> subscribe(final List<String> datapointNameList, String caller) {
		List<Datapoint> result = new ArrayList<>();
		datapointNameList.forEach(dp -> {
			try {
				this.getCell().getDataStorage().subscribeDatapoint(dp, caller);
				result.add(this.getCell().getDataStorage().readFirst(dp));
			} catch (Exception e) {
				log.error("Cannot subscribe datapoint={}", dp, e);
			}
		});

		return result;
	}

}
