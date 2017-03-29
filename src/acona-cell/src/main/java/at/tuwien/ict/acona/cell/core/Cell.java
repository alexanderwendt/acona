package at.tuwien.ict.acona.cell.core;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionHandler;
import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import jade.core.behaviours.Behaviour;

public interface Cell {

	public Communicator getCommunicator();

	/**
	 * Get data storage
	 * 
	 * @return data storage
	 */
	public DataStorage getDataStorage();

	/**
	 * Get activation handler for all activations, which control the cell
	 * internal bahviours
	 * 
	 * @return activation handler
	 */
	public CellFunctionHandler getFunctionHandler();

	/**
	 * Get cell name (not the JADE local name)
	 * 
	 * @return cell name
	 */
	public String getName();

	/**
	 * Get local name without IP number
	 * 
	 * @return
	 */
	public String getLocalName();

	/**
	 * Get a copy of the current cell configuration
	 * 
	 * @return
	 */
	public JsonObject getConfiguration();

	/**
	 * Proxy from JADE agent to add a JADE behaviour
	 * 
	 * @param b
	 */
	public void addBehaviour(Behaviour b);

	/**
	 * Reset current cellinternal behaviours and activators and setup new cell
	 * behaviours. It is a reset with new cell behaviours
	 * 
	 * @param conf:
	 *            Valid JsonObject for cells
	 * @throws Exception
	 */
	public void setupCellFunctions(CellConfig conf) throws Exception;

	/**
	 * Register a service
	 * 
	 * @param name
	 */
	public void registerService(String name);

	/**
	 * Close cell
	 */
	public void takeDownCell();

}
