package at.tuwien.ict.acona.framework.modules;

import java.util.Map;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ControllerFunction extends CellFunctionThreadImpl implements Controller {
	
	private int defaultTimeout = 1000;
	private String COMMANDDATAPOINT = this.getFunctionName() + "command";
	private int state = 0;

	@Override
	public AgentState sendCommandToAgent(String agentName, ControlCommand command) throws Exception {
		AgentState state  = AgentState.valueOf(this.getCommunicator().query(Datapoint.newDatapoint(COMMANDDATAPOINT).setValue(command.toString()), command.toString(), defaultTimeout).getValueAsString());
		
		return state;
		//Command is blocking
	}

	@Override
	protected void cellFunctionInternalInit() throws Exception {
		
	}

	@Override
	protected void updateDatapoint(Datapoint subscribedData) throws Exception {
		//React on the start trigger
		if (subscribedData.getAddress().equals(this.getSubscribedDatapoints().get(COMMANDDATAPOINT).getAddress())) {
			try {
				this.setCommand(subscribedData.getValue().getAsString());
			} catch (Exception e) {
				log.error("Cannot read command", e);
			}
		}
		
	}

	@Override
	protected void executeFunction() throws Exception {
		// TODO Auto-generated method stub
		
		//Wait for service
		
	}

	@Override
	public AgentState sendNonBlockingCommandToAgent(String agentName, ControlCommand command) throws Exception {
		this.getCommunicator().write(Datapoint.newDatapoint(COMMANDDATAPOINT).setValue(command.toString()), agentName);
		
		return AgentState.FINISHED;
	}

	@Override
	protected void executePostProcessing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executePreProcessing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateData(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
		
	}

}
