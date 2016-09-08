package at.tuwien.ict.acona.cell.core;

import java.util.Arrays;
import java.util.List;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.storage.DataStorage;

public class InspectorCellClient {
	private InspectorCell cell;
	
	public void setCellInspector(InspectorCell cell) {
		this.cell = cell;
	}
	
	public InspectorCell getCell() {
		return this.cell;
	}
	
	public void writeLocalDatapoint(Datapoint dp) throws Exception {
		this.cell.getCommunicator().write(dp);
	}
	
	public Datapoint readLocalDatapoint(String address) throws Exception {
		return this.getCell().getCommunicator().read(Datapoint.newDatapoint(address));
	}
	
	public Datapoint subscribeForeignDatapoint(String address, String agentName) throws Exception {
		List<Datapoint> list = this.getCell().getCommunicator().subscribe(Arrays.asList(Datapoint.newDatapoint(address)), agentName);
		
		Datapoint result = null;
		if (list.isEmpty()==false) {
			result = list.get(0);
		} else {
			throw new Exception("Nothing was read from the subscribption");
		}
		
		
		return result;
	}
	
	public void unsubscribeLocalDatapoint(String address, String agentName) throws Exception {
		this.getCell().getCommunicator().unsubscribe(Arrays.asList(Datapoint.newDatapoint(address)), agentName);
	}
	
	public DataStorage getDataStorage() {
		return this.cell.getDataStorage();
	}
}
