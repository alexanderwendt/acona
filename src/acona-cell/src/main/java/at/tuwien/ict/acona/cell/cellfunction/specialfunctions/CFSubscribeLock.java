package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CFSubscribeLock extends CellFunctionImpl {

	protected static Logger log = LoggerFactory.getLogger(CFSubscribeLock.class);

	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<>();

	private String resultAddress = "";

	public static Datapoint newServiceExecutionAndSubscribeLock(String agentName, String serviceName, List<Datapoint> serviceParameter, String resultAgentName, String resultAddress, int timeout, Cell cell) throws Exception {
		Datapoint result = null;

		CFSubscribeLock instance = new CFSubscribeLock();
		try {
			//create and register instance
			String name = "CFSubscribeLock_" + resultAgentName + ":" + resultAddress;
			log.trace("Service {}>Initialize with result={}:{}", name, resultAgentName, resultAddress);
			instance.init(CellFunctionConfig.newConfig(name, CFSubscribeLock.class).addManagedDatapoint(resultAddress, resultAddress, resultAgentName, SyncMode.SUBSCRIBEONLY), cell);

			//Execute the function method
			List<Datapoint> functionResult = instance.executeService(agentName, serviceName, serviceParameter, timeout);

			if (functionResult.isEmpty() == false && functionResult.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
				throw new Exception("Cannot execute the service=" + serviceName + " with the parameter=" + serviceParameter + " in the agent=" + agentName);
			}

			//Wait for subscribed value
			result = instance.waitForSubscription(resultAgentName, resultAddress, timeout);

			//Deregister
			//instance.shutDown();

		} catch (Exception e) {
			log.error("Query error");
			throw new Exception(e);
		} finally {
			//Deregister
			instance.shutDown();
		}

		return result;
	}

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
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

			this.queue.put(dp);

		} catch (InterruptedException e) {
			log.error("Service {}>Cannot receive data through subscription", this.getFunctionConfig().getName());
		}
	}

	private List<Datapoint> executeService(String agentName, String serviceName, List<Datapoint> serviceParameter, int timeout) throws Exception {
		return this.getCommunicator().execute(agentName, serviceName, serviceParameter, timeout);
	}

	private Datapoint waitForSubscription(String resultAgentName, String resultAddress, int timeout) throws Exception {
		Datapoint result = null;
		this.resultAddress = resultAddress;
		//this.resultAgent = resultAgentName;

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
				log.error("Service {}>Timeouterror", this.getFunctionConfig().getName());
				throw new Exception("Timeout on service " + this.getFunctionConfig().getName());
			}
		} catch (Exception e) {
			log.error("Service {}>Error on receiving data", this.getFunctionConfig().getName());
			throw new Exception(e);
		} finally {
			// If deregister has not been executed yet, do it
			//			if (isExitSet == false) {
			//				this.shutDown();
			//				this.isExitSet = true;
			//			}
		}

		return result;
	}

}
