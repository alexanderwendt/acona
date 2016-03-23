package at.tuwien.ict.acona.cell.core;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapackage;
import jade.core.behaviours.CyclicBehaviour;

public abstract class CellFunctionBehavior extends CyclicBehaviour {

	protected JsonObject settings;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Datapackage data;
	
	protected final Cell caller;
	
	public CellFunctionBehavior(Cell caller) {
		this.caller = caller;
	}
	
	public void init(JsonObject settings) {
		this.settings = settings;
	}
	
	public void setData(Datapackage data) {
		this.data = data;
	}
	
	@Override
	public void action() {
		//Execute function on restart
		this.function(data);
		
		//Block the behavior
		block();
	}
	
	public abstract void function(Datapackage data);

}
