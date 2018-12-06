package at.tuwien.ict.acona.mq.launcher;

import at.tuwien.ict.acona.mq.cell.core.Cell;

public interface SystemController {
	/**
	 * Interface between the external inputs to the system and the KORE system. Through the parameters, all user input is passed.
	 * 
	 * @param command
	 * @param parameter
	 */
	public void executeUserInput(String command, String parameter);

	/**
	 * Get the controller for an agent with a certain name
	 * 
	 * @param localName
	 * @return
	 */
	public Cell getAgent(String localName);

}
