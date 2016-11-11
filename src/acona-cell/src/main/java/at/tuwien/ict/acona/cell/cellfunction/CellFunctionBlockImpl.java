package at.tuwien.ict.acona.cell.cellfunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wendt
 * 
 *         The blocking executor does not use any thread and is a combination of
 *         some conditions with activate on any change of a datapoint that is
 *         subscribed and the behaviour itself. The blocking executor can only
 *         be executed if a subscribed datapoint is received
 *
 */
public abstract class CellFunctionBlockImpl extends CellFunctionExecutorImpl {

	private static Logger log = LoggerFactory.getLogger(CellFunctionBlockImpl.class);

	// protected static Logger log =
	// LoggerFactory.getLogger(CellFunctionThreadImpl.class);
	// private Thread t;

	// private int executeRate = 1000;

	/**
	 * Name of the activator
	 */
	// private String name;

	/**
	 * Cell, which executes this function
	 */
	// protected Cell cell;

	/**
	 * List of datapoints that shall be subscribed
	 */
	// private final Map<String, String> subscriptions = new HashMap<String,
	// String>(); //Variable, datapoint

	private boolean isActive = true;
	// private boolean executeOnce = true;
	// private ControlCommand command = ControlCommand.STOP;
	// private boolean isAllowedToRun = true;

	public CellFunctionBlockImpl() {

	}

	@Override
	public void cellFunctionInit() throws Exception {
		this.setExecuteOnce(true);
		// this.name = name;
		// this.cell = caller;
		// this.subscriptions.putAll(subscriptionMapping);

		try {
			// Execute internal init
			// cellFunctionInit(); //e.g. add subscriptions

			// Subscribe datapoints
			// this.getCommunicator().subscribe(this.getSubscribedDatapoints(),
			// cell.getName());
			// this.subscriptions.values().forEach(s->{
			// this.getCommunicator().subscribeDatapoint(s, cell.getName());
			// });

			cellFunctionInternalInit();

			// Create a thread from this class
			// t = new Thread(this, this.getFunctionName());
			// t.start();

			log.info("CellFunction {} initilized", this.getFunctionName());
		} catch (Exception e) {
			log.error("CellFunction {} could not be initialized", this.getFunctionName());
			throw new Exception(e.getMessage());
		}
	}

	// @Override
	// public boolean runActivation(Datapoint subscribedData) throws Exception {
	// //This is the notify or update function of the executor
	// this.updateDatapoint(subscribedData);
	//
	// return true; //In the customized activator, the activation always
	// triggers the notify function. Only one datapoint at the time can be
	// triggered, no lists
	// }

	protected abstract void cellFunctionInternalInit() throws Exception;

	// protected abstract void updateDatapoint(Datapoint subscribedData) throws
	// Exception;

	@Override
	protected abstract void executeFunction() throws Exception;

	public void executeRun() {
		log.debug("Start cell function {}", this.getFunctionName());

		// while(isActive==true) {
		// Stop the system at the end of the turn, if STOP command has been
		// given
		// executeWait();

		try {
			if (this.isAllowedToRun() == true) {
				executePreProcessing();

				executeFunction();

				executePostProcessing();
			}
		} catch (Exception e1) {
			log.error("Error in program execution", e1);
		}

		try {
			this.setCommand(ControlCommand.PAUSE.toString());
		} catch (Exception e) {
			log.error("Error setting pause", e);
		}
		// if (this.isExecuteOnce()==false) {
		// try {
		// Thread.sleep(this.getExecuteRate());
		// } catch (InterruptedException e) {
		// log.warn("Sleep was interrupted", e);
		// }
		// } else {
		// //Set datapoint as pause and set pause command here
		// try {
		// this.setCommand(ControlCommand.PAUSE.toString());
		// } catch (Exception e) {
		// log.error("Error setting pause", e);
		// }
		// }
		// }

		log.debug("Stop executor {}", this.getFunctionName());
	}

	@Override
	protected abstract void executePostProcessing() throws Exception;

	@Override
	protected abstract void executePreProcessing() throws Exception;

	// === Internal functions for the control of the tread ===//

	// /**
	// * Check, which command is valid and block until finished
	// */
	// private synchronized void executeWait() {
	// while(this.getCurrentCommand().equals(ControlCommand.STOP) ||
	// getCurrentCommand().equals(ControlCommand.PAUSE)) {
	// try {
	// //Block profile controller
	// this.setAllowedToRun(false);
	// this.wait();
	// } catch (InterruptedException e) {
	// log.trace("Wait interrupted client");
	// }
	// }
	// }

	protected synchronized void setCommand(String commandString) throws Exception {
		if (ControlCommand.isCommand(commandString)) {
			this.setCurrentCommand(ControlCommand.valueOf(commandString));
			setCommand(this.getCurrentCommand());
			log.info("Codelet {}: command {} set", this.getFunctionName(), this.getCurrentCommand());
		} else {
			log.warn("Command string is no command: {}", commandString);
		}
	}

	@Override
	public synchronized void setCommand(ControlCommand command) {
		this.setCurrentCommand(command);
		if (this.getCurrentCommand().equals(ControlCommand.START) == true) {
			this.setAllowedToRun(true);
			this.executeRun();
			// this.notify();
		} else if (this.getCurrentCommand().equals(ControlCommand.EXIT) == true) {
			this.setActive(false);
		}
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

}
