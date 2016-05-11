package at.tuwien.ict.acona.cell.core;

import at.tuwien.ict.acona.cell.storage.DataStorage;

public class InspectorCellClient {
	private InspectorCell cell;
	
	public void setCellInspector(InspectorCell cell) {
		this.cell = cell;
	}
	
	public InspectorCell getCell() {
		return this.cell;
	}
	
	public DataStorage getDataStorage() {
		return this.cell.getDataStorage();
	}
}
