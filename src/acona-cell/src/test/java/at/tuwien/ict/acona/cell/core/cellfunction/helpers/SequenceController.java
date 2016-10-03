package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class SequenceController extends CellFunctionThreadImpl {

	private String COMMANDDATAPOINTNAME = "command";
	
	@Override
	protected void cellFunctionInternalInit() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeFunction() throws Exception {
		//execute service 1
		this.getCommunicator().query(Datapoint.newDatapoint(this.getConfig().getProperty("Servicename") + ".command").setValue(ControlCommand.START.toString()), this.getConfig().getProperty("agent1"), Datapoint.newDatapoint(this.getConfig().getProperty("Servicename") + ".state"), this.getConfig().getProperty("agent1"), 100000);
		//execute service 2
		this.getCommunicator().query(Datapoint.newDatapoint(this.getConfig().getProperty("Servicename") + ".command").setValue(ControlCommand.START.toString()), this.getConfig().getProperty("agent2"), Datapoint.newDatapoint(this.getConfig().getProperty("Servicename") + ".state"), this.getConfig().getProperty("agent2"), 100000);
		//execute service 3
		this.getCommunicator().query(Datapoint.newDatapoint(this.getConfig().getProperty("Servicename") + ".command").setValue(ControlCommand.START.toString()), this.getConfig().getProperty("agent3"), Datapoint.newDatapoint(this.getConfig().getProperty("Servicename") + ".state"), this.getConfig().getProperty("agent3"), 100000);
	}

	@Override
	protected void executePostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executePreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
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
	
}
