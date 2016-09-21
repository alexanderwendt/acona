package at.tuwien.ict.acona.framework.modules;

import at.tuwien.ict.acona.cell.activator.cellfunction.ControlCommand;

public interface Controller {
	public AgentState sendCommandToAgent(String agentName, ControlCommand command) throws Exception;
	public AgentState sendNonBlockingCommandToAgent(String agentName, ControlCommand command) throws Exception;
}
