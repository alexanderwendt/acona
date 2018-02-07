package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * @author wendt
 * 
 *         Lock the thread while waiting for a subscribed value to arrive.
 *
 */
public class CFSubscribeLock extends CellFunctionImpl {

	protected static Logger log = LoggerFactory.getLogger(CFSubscribeLock.class);

	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<>();

	private String resultAddress = "";
	private JsonElement expectedResult;

	/**
	 * Execute a function and subscribe the result from a certain datapoint.
	 * 
	 * Example: Execute function codelethandler and lock the executing function until the state datapoint has the value finished.
	 * 
	 * @param agentName:
	 *            target agent name, where the executing function is
	 * @param serviceName:
	 *            target service name
	 * @param serviceParameter:
	 *            Request for the function to be executed
	 * @param resultAgentName:
	 *            Resulting datapoint agent name
	 * @param resultAddress:
	 *            Resulting address in an agent
	 * @param expectedResult:
	 *            Filter for what results are accepted. "null" is any result
	 * @param timeout:
	 *            Timeout, when to interrupt the request
	 * @param cell:
	 *            Executing cell.
	 * @return
	 * @throws Exception
	 */
	public Datapoint newServiceExecutionAndSubscribeLock(String agentName, String serviceName, JsonRpcRequest serviceParameter, String resultAgentName, String resultAddress, JsonElement expectedResult, int timeout, Cell cell) throws Exception {
		// public Datapoint newServiceExecutionAndSubscribeLock(String agentName, String serviceName, JsonRpcRequest serviceParameter, String resultAgentName, String resultAddress, int timeout, Cell cell)
		// throws Exception {
		Datapoint result = null;

		try {
			this.resultAddress = resultAddress;
			this.expectedResult = expectedResult;
			// create and register instance
			String name = "CFSubscribeLock_" + resultAgentName + ":" + this.resultAddress;
			log.trace("Service {}>Initialize with result={}:{}", name, resultAgentName, this.resultAddress);
			this.init(CellFunctionConfig.newConfig(name, CFSubscribeLock.class).addManagedDatapoint(this.resultAddress, this.resultAddress, resultAgentName, SyncMode.SUBSCRIBEONLY), cell);

			// Execute the function method
			JsonRpcResponse functionResult = this.executeService(agentName, serviceName, serviceParameter, timeout);

			if (functionResult.getError() != null) {
				throw new Exception("Cannot execute the service=" + serviceName + " with the parameter=" + serviceParameter + " in the agent=" + agentName);
			}

			// Wait for subscribed value
			result = this.waitForSubscription(resultAgentName, this.resultAddress, timeout);

		} catch (Exception e) {
			String extendedState = this.getCommunicator().read(agentName + ":" + serviceName + "." + CellFunctionCodelet.EXTENDEDSTATESUFFIX).getValueAsString();
			log.error("Query error={}. Extended state of the called service={}", extendedState, e.getMessage(), e);
			throw new Exception(e.getMessage());
		} finally {
			// Deregister
			this.shutDown();
		}

		return result;
	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void cellFunctionInit() throws Exception {

	}

	@Override
	protected void shutDownImplementation() {
		log.trace("Service {}>Shut down.", this.getFunctionConfig().getName());

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		try {
			log.debug("Service {}>Received update message for temp subscription={}", this.getFunctionConfig().getName(), data);
			Datapoint dp = data.get(resultAddress);

			if ((this.expectedResult != null) && (this.expectedResult.isJsonNull() == false)) {
				if (dp.getValue().equals(this.expectedResult)) {
					log.debug("Received trigger value={}", dp.getValue(), this.expectedResult);
					this.queue.put(dp);
				} else {
					log.debug("recieved a non-triggering value={}. Expected={}", dp.getValue(), this.expectedResult);
				}
			} else {
				this.queue.put(dp);
			}

		} catch (InterruptedException e) {
			log.error("Service {}>Cannot receive data through subscription", this.getFunctionConfig().getName());
		}
	}

	private JsonRpcResponse executeService(String agentName, String serviceName, JsonRpcRequest serviceParameter, int timeout) throws Exception {
		return this.getCommunicator().execute(agentName, serviceName, serviceParameter, timeout);
	}

	private Datapoint waitForSubscription(String resultAgentName, String resultAddress, int timeout) throws Exception {
		Datapoint result = null;
		this.resultAddress = resultAddress;
		// this.resultAgent = resultAgentName;

		try {
			try {
				log.trace("Service {}>Poll temp queue", this.getFunctionConfig().getName());
				result = this.queue.poll(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("Message received", result);
				throw new Exception(e.getMessage());
			}

			log.trace("Service {}>Result recieved={}", this.getFunctionConfig().getName(), result);
			if (result == null) {
				// If no value received, read the value instead and test it that way

				// Read the value directly
				Datapoint dp = this.getCommunicator().read(resultAgentName + ":" + resultAddress);
				if ((this.expectedResult != null) && (this.expectedResult.isJsonNull() == false)) {
					if (dp.getValue().equals(this.expectedResult)) {
						log.warn("Read matching trigger value={}", dp.getValue(), this.expectedResult);
						// result = dp;
					} else {
						log.warn("read a non-matching trigger value={}. Expected={}", dp.getValue(), this.expectedResult);
					}
				} else {
					// result = dp;
					log.warn("Read value={}", dp);
				}

				if (result == null) {
					log.error("Service {}>Timeouterror after {}ms. Expected datapoint={}, value={}", this.getFunctionConfig().getName(), timeout, resultAgentName + ":" + this.resultAddress, this.expectedResult);
					throw new Exception("Service " + this.getFunctionConfig().getName() + ">Timeouterror. Waiting to hear from address=" + resultAgentName + ":" + resultAddress);
				}
			}
		} catch (Exception e) {
			// log.error("Service {}>Error on receiving data", this.getFunctionConfig().getName());
			throw new Exception(e);
		} finally {
			// If deregister has not been executed yet, do it
			// if (isExitSet == false) {
			// this.shutDown();
			// this.isExitSet = true;
			// }
		}

		return result;
	}

}
