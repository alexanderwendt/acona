package at.tuwien.ict.acona.mq.core.core;

import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.AgentFunctionConfig;
import at.tuwien.ict.acona.mq.core.storage.DataStorage;

public interface Cell {

	/**
	 * Get cell name (not the JADE local name)
	 * 
	 * @return cell name
	 */
	public String getName();

//	/**
//	 * Get local name without IP number
//	 * 
//	 * @return
//	 */
//	public String getLocalName();

	/**
	 * Get the communicator
	 * 
	 * @return
	 */
	public MqttCommunicator getCommunicator();

	/**
	 * Get data storage
	 * 
	 * @return data storage
	 */
	public DataStorage getDataStorage();

	/**
	 * Get activation handler for all activations, which control the cell internal bahviours
	 * 
	 * @return activation handler
	 */
	public AgentFunctionHandler getFunctionHandler();

	/**
	 * Get a copy of the current cell configuration
	 * 
	 * @return
	 */
	public AgentConfig getConfiguration();

	/**
	 * Reset current cellinternal behaviours and activators and setup new cell behaviours. It is a reset with new cell behaviours
	 * 
	 * @param conf:
	 *            Valid JsonObject for cells
	 * @throws Exception
	 */
	public void init(AgentConfig conf) throws Exception;

	/**
	 * Add a new cell function to a running cell from a cellfunction config.
	 * 
	 * @param cellFunctionConfig
	 * @throws Exception
	 */
	public void addFunction(AgentFunctionConfig cellFunctionConfig) throws Exception;

	/**
	 * Add a new cell function to a running cell from a cellfunction config.
	 * 
	 * @param cellFunctionConfig
	 * @throws Exception
	 */
	public void removeCellFunction(String cellFunctionName) throws Exception;

	/**
	 * Close cell
	 */
	public void takeDownCell();

}
