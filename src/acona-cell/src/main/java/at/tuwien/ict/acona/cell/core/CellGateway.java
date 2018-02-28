package at.tuwien.ict.acona.cell.core;

import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.storage.DataStorage;

public interface CellGateway {

	/**
	 * Initializes the cell gateway with a cell. The gateway is used to access the
	 * cell outside of the cell
	 * 
	 * @param cell
	 */
	public void init(CellImpl cell);

	/**
	 * Get the communicator
	 * 
	 * @return
	 */
	public Communicator getCommunicator();

	/**
	 * Get the cell
	 * 
	 * @return
	 */
	public CellImpl getCell();

	/**
	 * Get the data storage to use for direct input in the database without
	 * communicator
	 * 
	 * @return
	 */
	public DataStorage getDataStorage();

	// /**
	// * @param key
	// * @param value
	// */
	// public void setCustomAgentSetting(String key, String value);

}
