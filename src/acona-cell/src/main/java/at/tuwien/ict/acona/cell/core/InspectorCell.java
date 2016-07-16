package at.tuwien.ict.acona.cell.core;

public class InspectorCell extends CellImpl {
	private InspectorCellClient controller;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void setup() {
		super.setup();
		
		Object[] args = this.getArguments();
		if (args!=null) {
			controller = ((InspectorCellClient)args[1]);	//Mode=0: return message in return message, Mode=1: append returnmessage, mode=2: return incoming message 
			
			log.debug("agent will use an inspector as controller");
		} else {
			throw new NullPointerException("No arguments found although necessary. Add inspectorcontroller");
		}
		
		controller.setCellInspector(this);
	}
}