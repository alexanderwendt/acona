package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.AconaFunctionService;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class SimpleControllerService extends AconaFunctionService {

	private static Logger log = LoggerFactory.getLogger(SimpleControllerService.class);

	private int delay = 200;

	// execute service 1

	// private ServiceState executeServiceById(String serviceNameId, String
	// agentNameId, int timeout) throws Exception {
	// return executeService(this.getConfig().getProperty(serviceNameId),
	// this.getConfig().getProperty(agentNameId),
	// timeout);
	// }
	//
	// private ServiceState executeService(String serviceName, String agentName,
	// int timeout) throws Exception {
	// String commandDatapoint = serviceName + ".command";
	// String resultDatapoint = serviceName + ".state";
	// Datapoint result1 = this.getCommunicator().query(
	// Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()),
	// agentName,
	// Datapoint.newDatapoint(resultDatapoint), agentName, timeout);
	//
	// return ServiceState.valueOf(result1.getValueAsString());
	// }

	@Override
	protected void executeFunction() throws Exception {
		try {
			String serviceName = this.getConfig().getProperty("servicename");
			String agentName = this.getConfig().getProperty("agentname");

			log.info("Start simple controller service to execute one service {} at agent {}", serviceName, agentName);

			Datapoint result = this.getCommunicator().query(Datapoint.newDatapoint(serviceName + ".command").setValue(ControlCommand.START.toString()), agentName,
					Datapoint.newDatapoint(serviceName + ".state"), agentName, 10000);
			log.debug("Service executed with the result={}", result);
		} catch (Exception e) {
			log.error("Cannot execute simple controller service", e);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	protected void serviceInit() {
		this.delay = Integer.valueOf(this.getConfig().getProperty("delay", String.valueOf(this.delay)));

	}

}
