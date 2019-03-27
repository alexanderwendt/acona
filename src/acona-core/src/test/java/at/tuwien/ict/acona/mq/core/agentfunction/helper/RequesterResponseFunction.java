package at.tuwien.ict.acona.mq.core.agentfunction.helper;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunction;
import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.core.core.DummyAgent;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class RequesterResponseFunction implements Runnable {

	private static Logger log = LoggerFactory.getLogger(RequesterResponseFunction.class);

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
	public RequesterResponseFunction() {

	}

	public void init(Semaphore sem, String host, String userName, String password, String agentName, String functionName, String targetAddress, boolean isRequester) throws Exception {
		this.responderFunctionAndMethod = targetAddress;
		this.isRequester = isRequester;
		this.semaphore = sem;

		// Map<String, Function<Request, Response>> methods = new HashMap<>();

		// Put the registered functions here
		// methods.put("increment", (Request input) -> increment(input));
		AgentFunction incrementFunction = new IncrementFunction();
		incrementFunction.init(FunctionConfig.newConfig(functionName, IncrementFunction.class), new DummyAgent(agentName));

		this.comm = incrementFunction.getCommunicator();
		// comm = new MqttCommunicatorImpl(new DataStorageImpl());
		// comm.init(host, userName, password, new CellFunctionDummy("OnlyForCommunicator" + this.hashCode(), agentName));

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

	public int getValue() {
		return this.value;
	}
}
