package at.tuwien.ict.acona.mq.cell.core;

import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.storage.DataStorage;

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
	public CellFunctionHandler getFunctionHandler();

	/**
	 * Get a copy of the current cell configuration
	 * 
	 * @return
	 */
	public CellConfig getConfiguration();

	/**
	 * Reset current cellinternal behaviours and activators and setup new cell behaviours. It is a reset with new cell behaviours
	 * 
	 * @param conf:
	 *            Valid JsonObject for cells
	 * @throws Exception
	 */
	public void init(CellConfig conf) throws Exception;

	/**
	 * Add a new cell function to a running cell from a cellfunction config.
	 * 
	 * @param cellFunctionConfig
	 * @throws Exception
	 */
	public void addCellFunction(CellFunctionConfig cellFunctionConfig) throws Exception;

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
