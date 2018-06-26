package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * This service notifies subscribers about changes in the data storage.
 * 
 * @author wendt
 *
 */
public class BasicServiceNotifySubscribers extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	private ExecutorService executor = Executors.newFixedThreadPool(5);

	@Override
	protected void basicServiceInit() throws Exception {
		super.basicServiceInit();

	}

	@Override
	public JsonRpcResponse performOperation(final JsonRpcRequest parameterdata, final String caller) {

		JsonRpcResponse result = null;

		try {
			String s = parameterdata.getParameter(0, new TypeToken<String>() {});
			final Datapoint dp = (new Gson()).fromJson(s, Datapoint.class);

			// Datapoint dp = parameterdata.getParameter(0, new TypeToken<Datapoint>() {
			// });

			// Implement a runnable in the method because the threads shall not block each other
			Runnable worker = new Runnable() {

				@Override
				public void run() {
					log.trace("Notify subscribers service for caller={}, addresses={}", caller, parameterdata.getParams());
					// dp.forEach(d -> {
					try {
						getCell().getSubscriptionHandler().activateNotifySubscribers(caller, dp);
					} catch (Exception e) {
						log.error("Error at the notification of subscriptions. Caller={}, Address={}, Request={}", caller, parameterdata, e);
					}

				}

			};

			executor.execute(worker);

			// log.trace("Notify subscribers service for caller={}, addresses={}", caller, parameterdata.getParams());
			// dp.forEach(d -> {
			// this.getCell().getSubscriptionHandler().activateNotifySubscribers(caller, dp);
			// });

			result = new JsonRpcResponse(parameterdata, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));

		} catch (Exception e) {
			log.error("Cannot perform notify on parameter={}", parameterdata, e);
			result = new JsonRpcResponse(parameterdata, new JsonRpcError("NotifyError", -1, e.getMessage(), e.getLocalizedMessage()));
		}

		return result;
	}

}
