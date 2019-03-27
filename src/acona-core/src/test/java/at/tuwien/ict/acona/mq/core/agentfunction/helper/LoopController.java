package at.tuwien.ict.acona.mq.core.agentfunction.helper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.ControlCommand;
import at.tuwien.ict.acona.mq.core.agentfunction.ServiceState;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class LoopController extends AgentFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(LoopController.class);

	private int delay = 200;

	// execute service 1

	private Response executeServiceById(String serviceNameId, String agentNameId, int number, int timeout) throws Exception {
		// ControllerCellGateway controllerMethods = new ControllerWrapper(this.getCommunicator());
		Response result = this.getCommunicator().execute(this.getFunctionConfig().getProperty(agentNameId) + number + ":" + this.getFunctionConfig().getProperty(serviceNameId) + "/" + "command", 
				(new Request())
				.setParameter("command", ControlCommand.START)
				.setParameter("blocking", true), 100000);
				
				//.executeServiceBlocking(this.getFunctionConfig().getProperty(agentNameId) + number + ":" + this.getFunctionConfig().getProperty(serviceNameId), timeout);
		

		return result;
	}

	@Override
	protected void executeFunction() throws Exception {
		log.info("Execute increment service");
		int numberOfAgents = this.getFunctionConfig().getProperty("numberofagents", Integer.class);

		for (int i = 1; i <= numberOfAgents; i++) {
			Response result = this.executeServiceById("servicename", "agentnameprefix", i, 5000);
			log.debug("Result = {}", result);

		}

		log.info("Loopcontroller finished");

	}

	@Override
	protected void shutDownThreadExecutor() {
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
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
