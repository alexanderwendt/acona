package at.tuwien.ict.acona.mq.cell.core;

import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.storage.DataStorage;

@Deprecated
public interface CellGateway {

	/**
	 * Initializes the cell gateway with a cell. The gateway is used to access the cell outside of the cell
	 * 
	 * @param cell
	 */
	public void init(CellImpl cell);

	/**
	 * Get the communicator
	 * 
	 * @return
	 */
	public MqttCommunicator getCommunicator();

	/**
	 * Get the cell
	 * 
	 * @return
	 */
	public CellImpl getCell();

	/**
	 * Get the data storage to use for direct input in the database without communicator
	 * 
	 * @return
	 */
	public DataStorage getDataStorage();

}
