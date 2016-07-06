package at.tuwien.ict.acona.cell.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;

public abstract class CellFunctionBehaviourImpl extends CyclicBehaviour implements CellFunctionBehaviour {

	protected static Logger log = LoggerFactory.getLogger(CellFunctionBehaviourImpl.class);
	protected String name;
	protected JsonObject conf;
	//protected final Activator activator;
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Datapoint> data= new HashMap<String, Datapoint>();
	private boolean isAllowedToRun=false;
	
	protected Cell caller;
	
	public CellFunctionBehaviourImpl() {
		
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public CellFunctionBehaviour init(String name, JsonObject conf, Cell caller) {
		this.name = name;
		this.conf = conf;
		this.isAllowedToRun = false;
		this.caller = caller;
		
		//this.subInit();
		
		return this;
	}
	
//	/**
//	 * Init of the children of this behaviour
//	 */
//	protected abstract void subInit();
	
	@Override
	public void setData(Map<String, Datapoint> data) {
		this.data = data;
		log.trace("Cell {}> data set={}", this.name, this.data);
	}
	
	@Override
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

	@Override
	public void startBehaviour() {
		//Proxy
		this.restart();
	}
	
	@Override
	public void addBehaviourToCallerCell(Cell caller) {
		//ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
		caller.addBehaviour(this);
	}
	
	protected void writeToDataStorage(Datapoint datapoint) {
		this.caller.getDataStorage().write(datapoint, caller.getName());
	}
	
	protected Datapoint readFromDataStorage(String address) {
		return this.caller.getDataStorage().read(address);
	}
	
	public abstract void function(Map<String, Datapoint> data);

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);
		builder.append(", settings=");
		builder.append(conf);
		builder.append(", data=");
		builder.append(data);
		builder.append(", caller=");
		builder.append(caller);
		return builder.toString();
	}



}
