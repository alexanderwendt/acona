package at.tuwien.ict.acona.cell.core;

import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface CellGateway {
	public void init(CellImpl cell);
	public Datapoint readLocalDatapoint(String address) throws Exception;
	public Datapoint subscribeForeignDatapoint(String address, String agentName) throws Exception;
	public void unsubscribeLocalDatapoint(String address, String agentName) throws Exception;
	public Communicator getCommunicator();
	public void setCustomAgentSetting(String key, String value);
}
