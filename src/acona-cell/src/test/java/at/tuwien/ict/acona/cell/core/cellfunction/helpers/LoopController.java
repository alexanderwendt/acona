package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.framework.interfaces.ControllerCellGateway;
import at.tuwien.ict.acona.framework.interfaces.ControllerWrapper;
import at.tuwien.ict.acona.framework.modules.AconaFunctionService;
import at.tuwien.ict.acona.framework.modules.ServiceState;

public class LoopController extends AconaFunctionService {

	private static Logger log = LoggerFactory.getLogger(LoopController.class);

	private int delay = 200;

	// execute service 1

	private ServiceState executeServiceById(String serviceNameId, String agentNameId, int number, int timeout) throws Exception {
		ControllerCellGateway controllerMethods = new ControllerWrapper(this.getCommunicator());
		ServiceState result = controllerMethods.executeService(this.getConfig().getProperty(agentNameId) + number, this.getConfig().getProperty(serviceNameId), new JsonObject(), timeout);

		return result;
	}

	// private ServiceState executeService(String serviceName, String agentName,
	// int timeout) throws Exception {
	//
	// String commandDatapoint = serviceName + ".command";
	// String resultDatapoint = serviceName + ".state";
	// Datapoint result1 =
	// this.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()),
	// agentName, Datapoint.newDatapoint(resultDatapoint),
	// agentName, timeout);
	//
	// return ServiceState.valueOf(result1.getValueAsString());
	// }

	@Override
	protected void executeFunction() throws Exception {
		log.info("Execute increment service");
		int numberOfAgents = this.getConfig().getProperty("numberofagents", Integer.class);

		for (int i = 1; i <= numberOfAgents; i++) {
			ServiceState result = this.executeServiceById("servicename", "agentnameprefix", i, 1000);
			log.debug("Result = {}", result);
			//			synchronized (this) {
			//				try {
			//					this.wait(delay);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}

		}

		log.info("Loopcontroller finished");

	}

	@Override
	protected void serviceInit() {
		// TODO Auto-generated method stub

	}

}
