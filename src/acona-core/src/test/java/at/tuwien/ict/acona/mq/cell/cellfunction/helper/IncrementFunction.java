package at.tuwien.ict.acona.mq.cell.cellfunction.helper;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class IncrementFunction extends CellFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void cellFunctionInit() throws Exception {
		this.addRequestHandlerFunction("increment", (Request input) -> increment(input));
		this.addRequestHandlerFunction("increment1", (Request input) -> increment(input));
		this.getCommunicator().setDefaultTimeout(1000);

		log.info("Created service class");

	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(String id, String topic, JsonElement data) {
		// TODO Auto-generated method stub

	}

	private Response increment(Request req) {
		log.debug("Increment the number in the request={}", req);
		Response result = new Response(req);

		try {
			int value = req.getParameter("input", Integer.class);
			value++;
			result.setResult(new JsonPrimitive(value));
			log.debug("Value incremented to {}", value);
		} catch (Exception e) {
			log.error("Cannot get value to increment");
			result.setError("Cannot increment string");
		}

		return result;

	}

}
