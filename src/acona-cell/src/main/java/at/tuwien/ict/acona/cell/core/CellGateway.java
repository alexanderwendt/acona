package at.tuwien.ict.acona.cell.core;

import java.util.Arrays;
import java.util.List;

import at.tuwien.ict.acona.cell.communicator.CommunicatorToCellFunction;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.storage.DataStorage;

public interface CellGateway {
	public Datapoint readLocalDatapoint(String address) throws Exception;
	public Datapoint subscribeForeignDatapoint(String address, String agentName) throws Exception;
	public void unsubscribeLocalDatapoint(String address, String agentName) throws Exception;
	public CommunicatorToCellFunction getCommunicator();
	public void setCustomSetting(String key, String value);
}
