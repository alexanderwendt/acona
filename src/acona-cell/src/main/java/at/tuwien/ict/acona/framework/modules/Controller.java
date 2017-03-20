package at.tuwien.ict.acona.framework.modules;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;

public interface Controller {
	public ServiceState sendCommandToAgent(String agentName, ControlCommand command) throws Exception;
	public ServiceState sendNonBlockingCommandToAgent(String agentName, ControlCommand command) throws Exception;
}
