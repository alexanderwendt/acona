package at.tuwien.ict.acona.cell.activator.cellfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class CellFunctionBlockImpl implements Activator  {
	
	protected static Logger log = LoggerFactory.getLogger(CellFunctionBlockImpl.class);
	
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
	public Activator initCellFunctions(String name, Map<String, String> subscriptionMapping, Cell caller) {
		this.name = name;
		this.cell = caller;
		this.subscriptions.putAll(subscriptionMapping);
		
		//Execute internal init
		cellFunctionInit();		//e.g. add subscriptions
		
		//Subscribe datapoints
		this.subscriptions.values().forEach(s->{
			this.cell.getDataStorage().subscribeDatapoint(s, cell.getName());
		});
		
		log.info("CellFunction {} initilized", this.getActivatorName());
		
		return this;
	}
	
	@Override
	public Activator initWithConditions(String name, Map<String, List<Condition>> subscriptionCondition, String logic, CellFunctionBehaviour behavior, Cell callerCell) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean runActivation(Datapoint subscribedData) {
		//This is the notify or update function of the executor
		this.updateDatapoint(subscribedData);
		
		return true;	//In the customized activator, the activation always triggers the notify function. Only one datapoint at the time can be triggered, no lists
	}
	
	protected abstract void cellFunctionInit();
	
	protected abstract void updateDatapoint(Datapoint subscribedData);
	
	protected abstract void executeFunction() throws Exception;
	
	public void run() {
		while(isActive==true) {
			//Stop the system at the end of the turn, if STOP command has been given
			executeWait();
			
			try {
				if (this.isAllowedToRun==true) {
					executeFunction();
				}
			} catch (Exception e1) {
				log.error("Error in program execution", e1);
			}
			
			if (this.executeOnce==false) {
				try {
					Thread.sleep(executeRate);
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

	protected Map<String, String> getSubscriptions() {
		return subscriptions;
	}

}
