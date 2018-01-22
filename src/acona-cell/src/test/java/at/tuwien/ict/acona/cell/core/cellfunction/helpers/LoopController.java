package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class LoopController extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(LoopController.class);

	private int delay = 200;

	// execute service 1

	private ServiceState executeServiceById(String serviceNameId, String agentNameId, int number, int timeout) throws Exception {
		// ControllerCellGateway controllerMethods = new ControllerWrapper(this.getCommunicator());
		ServiceState result = this.getCommunicator().executeServiceBlocking(this.getFunctionConfig().getProperty(agentNameId) + number + ":" + this.getFunctionConfig().getProperty(serviceNameId), timeout);

		return result;
	}

	@Override
	protected void executeFunction() throws Exception {
		log.info("Execute increment service");
		int numberOfAgents = this.getFunctionConfig().getProperty("numberofagents", Integer.class);

		for (int i = 1; i <= numberOfAgents; i++) {
			ServiceState result = this.executeServiceById("servicename", "agentnameprefix", i, 5000);
			log.debug("Result = {}", result);

		}

		log.info("Loopcontroller finished");

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void shutDownExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// this.writeLocal(Datapoints.newDatapoint("state").setValue(ServiceState.FINISHED.toString()));
		this.setServiceState(ServiceState.FINISHED);
		log.debug("finished loop controller post processing");

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
