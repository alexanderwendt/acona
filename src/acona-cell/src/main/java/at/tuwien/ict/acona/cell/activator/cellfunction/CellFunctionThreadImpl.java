package at.tuwien.ict.acona.cell.activator.cellfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.ActivatorConditionManager;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.activator.jadebehaviour.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

/**
 * @author wendt
 * 
 * The blocking executor does not use any thread and is a combination of some conditions with activate on any change of a datapoint
 * that is subscribed and the behaviour itself. The blocking executor can only be executed if a subscribed datapoint is received
 *
 */
public abstract class CellFunctionThreadImpl extends Thread implements Activator  {
	
	protected static Logger log = LoggerFactory.getLogger(CellFunctionThreadImpl.class);
	
	private int executeRate = 1000;
	
	/**
	 * Name of the activator
	 */
	private String name;
	
	/**
	 * Cell, which executes this function
	 */
	protected Cell cell;
	
	/**
	 * List of datapoints that shall be subscribed
	 */
	private final Map<String, String> subscriptions = new HashMap<String, String>();
	
	
	private boolean isActive = true;
	private boolean executeOnce = true;
	private ControlCommand command = ControlCommand.STOP;
	private boolean isAllowedToRun = true;
	
	@Override
	public Activator initCellFunctions(String name, Map<String, String> subscriptionMapping, Cell caller) throws Exception {
		this.name = name;
		this.cell = caller;
		this.subscriptions.putAll(subscriptionMapping);
		
		try {
			//Execute internal init
			cellFunctionInit();		//e.g. add subscriptions
			
			//Subscribe datapoints
			this.subscriptions.values().forEach(s->{
				this.cell.getDataStorage().subscribeDatapoint(s, cell.getName());
			});
			
			log.info("CellFunction {} initilized", this.getActivatorName());
		} catch (Exception e) {
			log.error("CellFunction {} could not be initialized", this.getActivatorName());
			throw new Exception(e.getMessage());
		}
		
		return this;
	}
	
	@Override
	public Activator initWithConditions(String name, Map<String, List<Condition>> subscriptionCondition, String logic, CellFunctionBehaviour behavior, Cell callerCell) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean runActivation(Datapoint subscribedData) throws Exception {
		//This is the notify or update function of the executor
		this.updateDatapoint(subscribedData);
		
		return true;	//In the customized activator, the activation always triggers the notify function. Only one datapoint at the time can be triggered, no lists
	}
	
	protected abstract void cellFunctionInit() throws Exception;
	
	protected abstract void updateDatapoint(Datapoint subscribedData) throws Exception;
	
	protected abstract void executeFunction() throws Exception;
	
	public void run() {
		while(isActive==true) {
			//Stop the system at the end of the turn, if STOP command has been given
			executeWait();
			
			try {
				if (this.isAllowedToRun==true) {
					executePreProcessing();
					
					executeFunction();
					
					executePostProcessing();
				}
			} catch (Exception e1) {
				log.error("Error in program execution", e1);
			}
			
			if (this.executeOnce==false) {
				try {
					sleep(executeRate);
				} catch (InterruptedException e) {
					log.warn("Sleep was interrupted", e);
				}
			} else {
				//Set datapoint as pause and set pause command here
				try {
					this.setCommand(ControlCommand.PAUSE.toString());
				} catch (Exception e) {
					log.error("Error setting pause", e);
				}
			}
		}
		
		log.debug("Stop executor {}", this.getActivatorName());
	}
	
	protected void executePostProcessing() {
		
	}
	
	protected void executePreProcessing() {
		
	}
	
	/**
	 * Check, which command is valid and block until finished
	 */
	private synchronized void executeWait() {
		while(this.command.equals(ControlCommand.STOP) || command.equals(ControlCommand.PAUSE)) {
			try {
				//Block profile controller
				this.wait();
			} catch (InterruptedException e) {
				log.trace("Wait interrupted client");
			}
		}
	}
	
	protected synchronized void setCommand(String commandString) throws Exception {
		if (ControlCommand.isCommand(commandString)) {
			this.command = ControlCommand.valueOf(commandString);
			setCommand(this.command);
			log.info("Codelet {}: command {} set", this.getActivatorName(), this.command);
		} else {
			log.warn("Command string is no command: {}", commandString);
		}
	}

	protected synchronized void setCommand(ControlCommand command) {
		this.command = command;
		if (this.command.equals(ControlCommand.START)==true) {
			this.notify();
		} else if (this.command.equals(ControlCommand.EXIT)==true) {
			this.closeActivator();
		}
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isExecuteOnceSet() {
		return executeOnce;
	}
	
	public void setExecuteOnce(boolean executeOnce) {
		this.executeOnce = executeOnce;
	}

	@Override
	public String getActivatorName() {
		return this.name;
	}

	@Override
	public List<String> getSubscribedDatapoints() {
		return new ArrayList<String>(this.subscriptions.values());
	}
	
	protected Map<String, String> getSubscriptions() {
		return subscriptions;
	}

	@Override
	public Map<String, List<ActivatorConditionManager>> getConditionMapping() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeActivator() {
		//If there is a thread, kill it
		this.setActive(false);
		
	}

	public int getExecuteRate() {
		return executeRate;
	}

	public void setExecuteRate(int blockingTime) {
		this.executeRate = blockingTime;
	}
	
	protected void writeLocal(Datapoint datapoint) throws Exception {
		this.cell.getCommunicator().write(datapoint);
	}
	
	protected <DATATYPE> void writeLocal(String address, DATATYPE datapoint) throws Exception {
		
		Gson gson = new Gson();
		String value = gson.toJson(datapoint);
		this.cell.getCommunicator().write(Datapoint.newDatapoint(address).setValue(value));
	}
	
	protected Datapoint readLocal(String address) throws Exception {
		return this.cell.getCommunicator().read(Datapoint.newDatapoint(address));
	}
	
	protected JsonElement readLocalAsJson(String address) throws Exception {
		return this.cell.getCommunicator().read(Datapoint.newDatapoint(address)).getValue();
	}
	
	protected String getCustomSetting(String key) {
		return this.cell.getConfiguration().get(key).getAsString();
	}

}
