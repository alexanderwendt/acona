package at.tuwien.ict.acona.cell.activator.jadebehaviour;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.CyclicBehaviour;

public abstract class CellFunctionBehaviourImpl extends CyclicBehaviour implements CellFunctionBehaviour {

	protected static Logger log = LoggerFactory.getLogger(CellFunctionBehaviourImpl.class);
	protected String name;
	protected JsonObject conf;
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Datapoint> data= new HashMap<String, Datapoint>();
	private boolean isAllowedToRun=false;
	private boolean runOnce = false;
	private long wakeupTime;
	private long period = 1000;
	
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
		
		this.subInit();
		
		return this;
	}
	
	/**
	 * Init of the children of this behaviour
	 */
	protected void subInit() {
		//Override this
	}
	
	@Override
	public void updateData(Map<String, Datapoint> data) {
		this.data.putAll(data);
		//this.setRunPermission(true); //is inside of startBehaviour
		this.startBehaviour();
		log.trace("Cell {}> data set={}", this.name, this.data);
	}
	
	@Override
	public void setRunPermission(boolean isAllowedToRun) {
		this.isAllowedToRun=isAllowedToRun;
	}
	
	private boolean hasRunPermission() {
		return this.isAllowedToRun;
	}
	
	@Override
	public void action() {
		//The behavior is always launched at the start. Block only pauses the behavior for the next time.
		log.trace("Behaviour {}>run allowed={}", this.name, this.hasRunPermission());
		
		long blockTime = -1;
		if (this.runOnce==false) {
			blockTime = wakeupTime - System.currentTimeMillis();
		}
		
		if (this.hasRunPermission()==true && blockTime<0) {
			log.trace("Behaviour {}> Execute with data={}", this.name, this.data);
			
			//Run the main function
			this.function(data);
			
			//Post processing to clean up for the next run
			this.postProcessData();	
			
			long currentTime = System.currentTimeMillis();
			wakeupTime = currentTime + period;	//If run, the next wakup will be in now+period
			blockTime = wakeupTime - currentTime;	//The time to wait is wakup - currenttime
		}

		if (myAgent != null) {
			if (this.runOnce==true) {
				this.setRunPermission(false);	//No more runs are allowed
				block();
			} else {
				this.setRunPermission(true);	//new run allowed
				block(blockTime);
			}	
		}
	}

	@Override
	public void startBehaviour() {
		//Proxy
		this.setRunPermission(true);
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
	
	public void postProcessData() {
		//Clean up behavior
		//this.setRunPermission(false);
		this.data.clear();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);
		//builder.append(", settings=");
		//builder.append(conf);
		//builder.append(", data=");
		//builder.append(data);
		builder.append(", caller=");
		builder.append(caller.getName());
		return builder.toString();
	}

}
