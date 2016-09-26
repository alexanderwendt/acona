package at.tuwien.ict.acona.cell.cellfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import _OLD.at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.cellfunction.special.ActivatorConditionManager;
import at.tuwien.ict.acona.cell.cellfunction.special.Condition;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

/**
 * @author wendt
 * 
 * The blocking executor does not use any thread and is a combination of some conditions with activate on any change of a datapoint
 * that is subscribed and the behaviour itself. The blocking executor can only be executed if a subscribed datapoint is received
 *
 */
public abstract class CellFunctionCyclicBehaviourImpl extends CellFunctionImpl  {

//	//protected static Logger log = LoggerFactory.getLogger(CellFunctionCyclicBehaviourImpl.class);
//	
//	//private int executeRate = 1000;
//	
//	/**
//	 * Name of the activator
//	 */
//	//private String name;
//	
//	/**
//	 * Cell, which executes this function
//	 */
//	//protected Cell cell;
//	
//	/**
//	 * List of datapoints that shall be subscribed
//	 */
//	private final Map<String, String> subscriptions = new HashMap<String, String>();
//	
//	//private boolean isActive = true;
//	private boolean executeOnce = true;
//	private ControlCommand command = ControlCommand.STOP;
//	private boolean isAllowedToRun = true;
	
	private long wakeupTime;
	private Behaviour behaviour;
	

	@Override
	public void cellFunctionInit() {
		this.behaviour = new ExecuteBehaviour();
		this.getCell().addBehaviour(behaviour);
	}
	
//	@Override
//	public boolean runActivation(Datapoint subscribedData) {
//		//This is the notify or update function of the executor
//		this.updateDatapoint(subscribedData);
//		
//		return true;	//In the customized activator, the activation always triggers the notify function. Only one datapoint at the time can be triggered, no lists
//	}
	
	protected abstract void updateDatapoint(Datapoint subscribedData);
	
	protected abstract void executeFunction() throws Exception;
	
	protected abstract void executePostProcessing();
	
	protected abstract void executePreProcessing();
	
	protected synchronized void setCommand(String commandString) throws Exception {
		if (ControlCommand.isCommand(commandString)) {
			this.setCurrentCommand(ControlCommand.valueOf(commandString));
			//setCommand(this.command);
			log.info("Codelet {}: command {} set", this.getFunctionName(), getCurrentCommand());
		} else {
			log.warn("Command string is no command: {}", commandString);
		}
	}

	public synchronized void setCommand(ControlCommand command) {
		this.setCurrentCommand(command);
		if (this.getCurrentCommand().equals(ControlCommand.START)==true) {
			this.setAllowedToRun(true);
			this.behaviour.restart();	//Restart cyclic behaviour
		} else if (this.getCurrentCommand().equals(ControlCommand.EXIT)==true) {
			this.behaviour.done();
		} else if (this.getCurrentCommand().equals(ControlCommand.PAUSE)==true) {
			this.setAllowedToRun(false);
		} else if (this.getCurrentCommand().equals(ControlCommand.STOP)==true) {
			this.setAllowedToRun(false);
		}
	}
	
	private class ExecuteBehaviour extends CyclicBehaviour {

		@Override
		public void action() {
			//The behavior is always launched at the start. Block only pauses the behavior for the next time.
			try {
				log.trace("Behaviour {}>run allowed={}", getFunctionName(), isAllowedToRun());
			
				long blockTime = -1;
				if (isExecuteOnce()==false) {
					blockTime = wakeupTime - System.currentTimeMillis();
				}
					
				if (isAllowedToRun()==true && blockTime<0) {
					log.trace("Execute behaviour {}", getFunctionName());
					//Execute preprocessing before the real function starts
					executePreProcessing();
				
					//Run the main function
					executeFunction();
					
					//Post processing to clean up for the next run
					executePostProcessing();	
						
					long currentTime = System.currentTimeMillis();
					wakeupTime = currentTime + getExecuteRate();	//If run, the next wakup will be in now+period
					blockTime = wakeupTime - currentTime;	//The time to wait is wakup - currenttime
				}

				if (myAgent != null) {
					if (isExecuteOnce()==true) {
						setCommand(ControlCommand.PAUSE);	//No more runs are allowed
						block();
					} else {
						setCommand(ControlCommand.START);	//new run allowed
						block(blockTime);
					}	
				}
			} catch (Exception e) {
				log.error("Cell function error");
			}
			
		}
		
	}

}
