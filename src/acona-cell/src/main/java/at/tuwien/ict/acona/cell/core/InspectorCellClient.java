package at.tuwien.ict.acona.cell.core;

public class InspectorCellClient {
	private InspectorCell cell;
	
	public void setCellInspector(InspectorCell cell) {
		this.cell = cell;
	}
	
	public InspectorCell getCell() {
		return this.cell;
	}
}
