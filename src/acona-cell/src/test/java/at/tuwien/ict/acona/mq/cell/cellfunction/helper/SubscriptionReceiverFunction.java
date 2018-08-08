package at.tuwien.ict.acona.mq.cell.cellfunction.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionDummy;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicatorImpl;
import at.tuwien.ict.acona.mq.cell.storage.DataStorageImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class SubscriptionReceiverFunction implements Runnable {

	private static Logger log = LoggerFactory.getLogger(SubscriptionReceiverFunction.class);

	private MqttCommunicator comm;
	private Thread t;

	private String responderFunctionAndMethod;
	private boolean isRequester;
	private int value = 0;
	private int numberOfRuns = 2000;

	private Semaphore semaphore;

	/**
	 * @param host
	 * @param userName
	 * @param password
	 * @param agentName
	 * @param functionName
	 * @param responderFunctionAndMethod
	 * @throws Exception
	 */
	public SubscriptionReceiverFunction() {

	}

	public void init(Semaphore sem, String host, String userName, String password, String agentName, String functionName, String responderFunctionAndMethod, boolean isRequester) throws Exception {
		this.responderFunctionAndMethod = responderFunctionAndMethod;
		this.isRequester = isRequester;
		this.semaphore = sem;

		Map<String, Function<Request, Response>> methods = new HashMap<>();
		methods.put("increment", (Request input) -> increment(input));

		CellFunction incrementFunction =

				comm = new MqttCommunicatorImpl(new DataStorageImpl());
		comm.init(host, userName, password, agentName, new CellFunctionDummy(functionName), methods);

		// Add functions
		// comm.addRequestHandlerFunction("test", (Request input) -> testFunction(input));
		// comm.addRequestHandlerFunction("increment", (Request input) -> increment(input));

		t = new Thread(this, agentName + "/" + functionName);
		t.start();
	}

	public void setNumberOfRuns(int number) {
		this.numberOfRuns = number;
	}

	@Override
	public void run() {

		if (this.isRequester == true) {
			for (int i = 0; i < this.numberOfRuns; i++) {
				// Add response function

				Request req = new Request();
				req.setParameter("input", value);
				Response result = null;

				try {
					log.debug("Send request={}", req);
					result = comm.execute(this.responderFunctionAndMethod, req);
					value = result.getResult().getAsInt();
					log.debug("Received value={}", value);
				} catch (Exception e) {
					log.error("Cannot send request", result, e);
				}
			}

			this.semaphore.release();
		} else {
			try {
				this.semaphore.acquire();
				this.semaphore.release();
			} catch (InterruptedException e) {
				log.error("Cannot aquire semaphore", e);
			}
		}

		log.debug("Received total value={}", value);
	}

//	private JsonElement testFunction(Request req) {
//		log.debug("Execute method testFunction");
//		return new JsonPrimitive("Method executed in the method testFunction");
//
//	}

	private Response increment(Request req) {
		log.debug("Increment the number in the request={}", req);
		Response result = new Response(req);

		try {
			int value = req.getParameter("input", Integer.class);
			value++;
			result.setResult(new JsonPrimitive(value));
		} catch (Exception e) {
			log.error("Cannot get value to increment");
			result.setError("Cannot increment string");
		}

		return result;

	}

	public int getValue() {
		return this.value;
	}
}
