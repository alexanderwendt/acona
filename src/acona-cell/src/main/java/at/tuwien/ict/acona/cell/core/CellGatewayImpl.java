package at.tuwien.ict.acona.cell.core;

import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.storage.DataStorage;

public class CellGatewayImpl implements CellGateway {
	private CellImpl cell;

	@Override
	public void init(CellImpl cell) {
		this.cell = cell;
	}

	public CellImpl getCell() {
		return this.cell;
	}

	@Override
	public void writeLocalDatapoint(Datapoint dp) throws Exception {
		this.cell.getCommunicator().write(dp);
	}

	@Override
	public Datapoint readLocalDatapoint(String address) throws Exception {
		return this.getCommunicator().read(address);
	}

	//	@Override
	//	public Datapoint subscribeForeignDatapoint(String address, String agentName) throws Exception {
	//		//create a temporary function and add to the cell
	//		//Check if 
	//		
	//		
	//		List<Datapoint> list = this.getCommunicator().subscribe(Arrays.asList(address), agentName);
	//
	//		Datapoint result = null;
	//		if (list.isEmpty() == false) {
	//			result = list.get(0);
	//		} else {
	//			throw new Exception("Nothing was read from the subscribption");
	//		}
	//
	//		return result;
	//	}

	//	@Override
	//	public void unsubscribeLocalDatapoint(String address, String agentName) throws Exception {
	//		this.getCell().getCommunicator().unsubscribe(Arrays.asList(address), agentName);
	//	}

	@Override
	public Communicator getCommunicator() {
		return this.getCell().getCommunicator();
	}

	@Override
	public void setCustomAgentSetting(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataStorage getDataStorage() {
		return this.getCell().getDataStorage();
	}
}
