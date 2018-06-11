package at.tuwien.ict.acona.mq.cell.cellfunction;

import at.tuwien.ict.acona.cell.communicator.BasicServiceCommunicator;
import at.tuwien.ict.acona.cell.communicator.CellFunctionHandler;
import at.tuwien.ict.acona.cell.communicator.SubscriptionHandler;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import jade.core.behaviours.Behaviour;

public interface MqttCell {

	public BasicServiceCommunicator getCommunicator();

	/**
	 * Get data storage
	 * 
	 * @return data storage
	 */
	public DataStorage getDataStorage();

	/**
	 * Get activation handler for all activations, which control the cell internal
	 * bahviours
	 * 
	 * @return activation handler
	 */
	public CellFunctionHandler getFunctionHandler();

	/**
	 * Get subscription handler to register, deregister or activate subscribed
	 * datapoints
	 * 
	 * @return
	 */
	public SubscriptionHandler getSubscriptionHandler();

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
	public CellConfig getConfiguration();

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
	 * Add a new cell function to a running cell from a cellfunction config.
	 * 
	 * @param cellFunctionConfig
	 * @throws Exception
	 */
	public void addCellFunction(CellFunctionConfig cellFunctionConfig) throws Exception;

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
