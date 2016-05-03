package at.tuwien.ict.acona.cell.core;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapackage;
import jade.core.behaviours.CyclicBehaviour;

public abstract class CellFunctionBehavior extends CyclicBehaviour {

	protected final String name;
	protected JsonObject settings;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Datapackage data;
	
	protected final Cell caller;
	
	public CellFunctionBehavior(String name, Cell caller) {
		this.caller = caller;
		this.name = name;
	}
	
	public void init(JsonObject settings) {
		this.settings = settings;
	}
	
	public void setData(Datapackage data) {
		this.data = data;
	}
	
	@Override
	public void action() {
		//Block the behavior
		block();
		//Execute function on restart
		this.function(data);
	}
	
	public abstract void function(Datapackage data);

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);
		builder.append(", settings=");
		builder.append(settings);
		builder.append(", data=");
		builder.append(data);
		builder.append(", caller=");
		builder.append(caller);
		return builder.toString();
	}

}
