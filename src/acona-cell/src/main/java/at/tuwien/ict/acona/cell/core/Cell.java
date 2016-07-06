package at.tuwien.ict.acona.cell.core;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.activator.ActivationHandler;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import jade.core.behaviours.Behaviour;

public interface Cell {
	
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
	public ActivationHandler getActivationHandler();
	
	/**
	 * Get cell name (not the JADE local name)
	 * 
	 * @return cell name
	 */
	public String getName();
	
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
	 * Reset current cellinternal behaviours and activators and setup new cell behaviours. It is a reset with new cell behaviours
	 * 
	 * @param conf: Valid JsonObject for cells
	 * @throws Exception 
	 */
	public void setupCellFunctionBehaviours(JsonObject conf) throws Exception;
	
	
	/**
	 * Get cell utils with send methods
	 * 
	 * @return
	 */
	public CellUtil getCellUtil();
}
