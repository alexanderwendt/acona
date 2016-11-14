package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.modules.ServiceState;

public class SequenceController extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(SequenceController.class);

	private String COMMANDDATAPOINTNAME = "command";
	private int delay = 200;

	// execute service 1

	private ServiceState executeServiceById(String serviceNameId, String agentNameId, int timeout) throws Exception {
		return executeService(this.getFunctionConfig().getProperty(serviceNameId), this.getFunctionConfig().getProperty(agentNameId), timeout);
	}

	private ServiceState executeService(String serviceName, String agentName, int timeout) throws Exception {
		String commandDatapoint = serviceName + ".command";
		String resultDatapoint = serviceName + ".state";
		Datapoint result1 = this.getCommunicator().queryDatapoints(commandDatapoint, new JsonPrimitive(ControlCommand.START.toString()), agentName, resultDatapoint, agentName, timeout);

		return ServiceState.valueOf(result1.getValueAsString());
	}

	@Override
	protected void executeFunction() throws Exception {
		// this.getConfig().getProperty("servicename")

		// String commandDatapoint = this.getConfig().getProperty("servicename")
		// + ".command";
		// String agent1 = this.getConfig().getProperty("agent1");

		// String resultDatapoint = serviceName + ".state";
		// log.debug("read the following values from the config={}, {}, {}",
		// commandDatapoint, agent1, resultDatapoint);
		ServiceState result1 = this.executeServiceById("servicename", "agent1", 100000);
		// Datapoint result1 =
		// this.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()),
		// agent1, Datapoint.newDatapoint(resultDatapoint), agent1, 100000);

		synchronized (this) {
			try {
				this.wait(delay);
			} catch (InterruptedException e) {

			}
		}

		log.debug("Result = {}", result1);
		// execute service 2
		// this.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()),
		// this.getConfig().getProperty("agent2"),
		// Datapoint.newDatapoint(resultDatapoint),
		// this.getConfig().getProperty("agent2"), 100000);
		ServiceState result2 = this.executeServiceById("servicename", "agent2", 100000);

		synchronized (this) {
			try {
				this.wait(delay);
			} catch (InterruptedException e) {

			}
		}
		// execute service 3
		// this.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()),
		// this.getConfig().getProperty("agent3"),
		// Datapoint.newDatapoint(resultDatapoint),
		// this.getConfig().getProperty("agent3"), 100000);

		ServiceState result3 = this.executeServiceById("servicename", "agent3", 100000);
		synchronized (this) {
			try {
				this.wait(delay);
			} catch (InterruptedException e) {

			}
		}

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		if (data.containsKey(COMMANDDATAPOINTNAME)
				&& data.get(COMMANDDATAPOINTNAME).getValueAsString().equals("{}") == false) {
			try {
				this.setCommand(data.get(COMMANDDATAPOINTNAME).getValueAsString());
			} catch (Exception e) {
				log.error("Cannot start system. Command is {}", data, e);
			}
		}
	}

	@Override
	protected void cellFunctionInternalInit() throws Exception {
		this.delay = Integer.valueOf(this.getFunctionConfig().getProperty("delay", String.valueOf(this.delay)));

	}

	@Override
	protected void executePostProcessing() throws Exception {
		this.writeLocal(Datapoint.newDatapoint("state").setValue(ServiceState.STOPPED.toString()));

	}

	@Override
	protected void executePreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

}
