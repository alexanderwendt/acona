package at.tuwien.ict.acona.cell.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.CyclicBehaviour;

public abstract class CellFunctionBehavior extends CyclicBehaviour {

	protected static Logger log = LoggerFactory.getLogger(CellFunctionBehavior.class);
	protected final String name;
	protected JsonObject settings;
	//protected final Activator activator;
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Datapoint> data= new HashMap<String, Datapoint>();
	private boolean isAllowedToRun=false;
	
	protected final Cell caller;
	
	public CellFunctionBehavior(String name, Cell caller) {
		this.caller = caller;
		this.name = name;
	}
	
	public void init(JsonObject settings) {
		this.settings = settings;
		this.isAllowedToRun = false;
	}
	
	public void setData(Map<String, Datapoint> data) {
		this.data = data;
		log.trace("Cell {}> data set={}", this.name, this.data);
	}
	
	public void setRunPermission(boolean isAllowedToRun) {
		this.isAllowedToRun=isAllowedToRun;
	}
	
	private boolean isAllowedToRun() {
		return this.isAllowedToRun;
	}
	
	@Override
	public void action() {
		//The behavior is always launched at the start. Block only pauses the behavior for the next time.
		log.trace("Behavior {}>run allowed={}", this.name, this.isAllowedToRun());
		if (this.isAllowedToRun()==true) {
			log.trace("Behavior {}> Execute with data={}", this.name, this.data);
			this.function(data);
			
			//Clean up behavior
			this.setRunPermission(false);
			this.data=null;
		} else {
			block();
		}		
	}
	
	public abstract void function(Map<String, Datapoint> data);

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
