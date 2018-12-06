package at.tuwien.ict.acona.mq.cell.cellfunction.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.mq.datastructures.ControlCommand;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;

public class SequenceController extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(SequenceController.class);

	private int delay = 200;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.delay = Integer.valueOf(this.getFunctionConfig().getProperty("delay", String.valueOf(this.delay)));
	}

	@Override
	protected void executeFunction() throws Exception {
		ServiceState result1 = this.executeServiceById("servicename", "agent1", 1000);

		synchronized (this) {
			try {
				this.wait(delay);
			} catch (InterruptedException e) {

			}
		}

		log.debug("Result1 = {}", result1);
		ServiceState result2 = this.executeServiceById("servicename", "agent2", 1000);
		log.debug("Result2 = {}", result2);
		synchronized (this) {
			try {
				this.wait(delay);
			} catch (InterruptedException e) {

			}
		}

		ServiceState result3 = this.executeServiceById("servicename", "agent3", 1000);
		log.debug("Result3 = {}", result3);
		synchronized (this) {
			try {
				this.wait(delay);
			} catch (InterruptedException e) {

			}
		}

		log.info("Function sequence controller finished");

	}

	/**
	 * This function loads the agent ids directly from the configuration
	 * 
	 * @param serviceNameId
	 * @param agentNameId
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	private ServiceState executeServiceById(String serviceNameId, String agentNameId, int timeout) throws Exception {
		return executeService(this.getFunctionConfig().getProperty(serviceNameId), this.getFunctionConfig().getProperty(agentNameId), timeout);
	}

	private ServiceState executeService(String serviceName, String agentName, int timeout) throws Exception {
		String commandDatapoint = this.getDatapointBuilder().generateCellTopic(agentName) + "/" + serviceName + "/command";
		String resultDatapoint = this.getDatapointBuilder().generateCellTopic(agentName) + "/" + serviceName + "/state";
		log.debug("Execute service={}", serviceName);
		Datapoint result1 = this.getCommunicator().executeRequestBlockForResult(commandDatapoint, (new Request()).setParameter("command", ControlCommand.START.toString()), resultDatapoint, new JsonPrimitive(ServiceState.FINISHED.toString()));

		log.debug("Service={} executed. Result={}", commandDatapoint, result1);
		return ServiceState.valueOf(result1.getValueAsString());
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		log.debug("{}> Start postprocessing", this.getFunctionRootAddress());

		// this.writeLocal(Datapoints.newDatapoint("state").setValue(ServiceState.FINISHED.toString()));
		log.debug("Set finished state");
		this.setServiceState(ServiceState.FINISHED);
		log.debug("Finished state set");

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		log.debug("{}> Start preprocessing", this.getFunctionRootAddress());

	}

	@Override
	protected void shutDownThreadExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub

	}

}
