package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.modules.AconaServiceWithSubscribers;
import at.tuwien.ict.acona.framework.modules.ServiceState;

public class SequenceController extends CellFunctionThreadImpl {

	private String COMMANDDATAPOINTNAME = "command";

	@Override
	protected void executeFunction() throws Exception {
		//execute service 1
		String commandDatapoint = this.getConfig().getProperty("servicename") + ".command";
		String agent1 = this.getConfig().getProperty("agent1");
		String resultDatapoint = this.getConfig().getProperty("servicename") + ".state";
		
		log.debug("read the following values from the config={}, {}, {}", commandDatapoint, agent1, resultDatapoint);
		Datapoint result1 = this.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()), agent1, Datapoint.newDatapoint(resultDatapoint), agent1, 100000);
		
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
		
		log.debug("Result = {}", result1);
		//execute service 2
		this.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()), this.getConfig().getProperty("agent2"), Datapoint.newDatapoint(resultDatapoint), this.getConfig().getProperty("agent2"), 100000);
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
		//execute service 3
		this.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()), this.getConfig().getProperty("agent3"), Datapoint.newDatapoint(resultDatapoint), this.getConfig().getProperty("agent3"), 100000);
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
		
		
	}
	

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		if (data.containsKey(COMMANDDATAPOINTNAME) && data.get(COMMANDDATAPOINTNAME).getValueAsString().equals("{}")==false) {
			try {
				this.setCommand(data.get(COMMANDDATAPOINTNAME).getValueAsString());
			} catch (Exception e) {
				log.error("Cannot start system. Command is {}", data, e);
			}
		}	
	}

	@Override
	protected void cellFunctionInternalInit() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executePostProcessing() throws Exception {
		this.writeLocal(Datapoint.newDatapoint("state").setValue(ServiceState.STOPPED.toString()));
		
	}

	@Override
	protected void executePreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
