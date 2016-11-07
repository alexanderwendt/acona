package at.tuwien.ict.acona.cell.cellfunction;

import java.lang.Thread.State;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

/**
 * @author wendt
 * 
 *         The blocking executor does not use any thread and is a combination of
 *         some conditions with activate on any change of a datapoint that is
 *         subscribed and the behaviour itself. The blocking executor can only
 *         be executed if a subscribed datapoint is received
 *
 */
public abstract class CellFunctionThreadImpl extends CellFunctionImpl implements Runnable {

	private static Logger log = LoggerFactory.getLogger(CellFunctionThreadImpl.class);

	private Thread t;

	private boolean isActive = true;

	public CellFunctionThreadImpl() {

	}

	@Override
	public void cellFunctionInit() throws Exception {
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
			t = new Thread(this, this.getCell().getLocalName() + "#" + this.getFunctionName());
			t.start();

			log.info("CellFunction {} initilized", this.getFunctionName());
		} catch (Exception e) {
			log.error("CellFunction {} could not be initialized", this.getFunctionName());
			throw new Exception(e.getMessage());
		}
	}

	protected abstract void cellFunctionInternalInit() throws Exception;

	@Override
	protected abstract void executeFunction() throws Exception;

	@Override
	public void run() {
		log.debug("Start cell function {}", this.getFunctionName());

		while (isActive == true) {
			// Stop the system at the end of the turn, if STOP command has been
			// given
			executeWait();

			try {
				if (this.isAllowedToRun() == true) {
					executePreProcessing();

					executeFunction();

					executePostProcessing();
				}
			} catch (Exception e1) {
				log.error("Error in program execution", e1);
			}

			if (this.isExecuteOnce() == false) {
				try {
					Thread.sleep(this.getExecuteRate());
				} catch (InterruptedException e) {
					log.warn("Sleep was interrupted", e);
				}
			} else {
				// Set datapoint as pause and set pause command here
				try {
					this.setCommand(ControlCommand.PAUSE.toString());
				} catch (Exception e) {
					log.error("Error setting pause", e);
				}
			}
		}

		log.debug("Stop executor {}", this.getFunctionName());
	}

	@Override
	protected abstract void executePostProcessing() throws Exception;

	@Override
	protected abstract void executePreProcessing() throws Exception;

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// If the thread is running, the method shall wait or produce timeout
		while (!t.getState().equals(State.WAITING)) {

		}

		this.updateDatapointsByIdOnThread(data);
	}

	protected abstract void updateDatapointsByIdOnThread(Map<String, Datapoint> data);

	// === Internal functions for the control of the tread ===//

	/**
	 * Check, which command is valid and block until finished
	 */
	private synchronized void executeWait() {
		while (this.getCurrentCommand().equals(ControlCommand.STOP)
				|| getCurrentCommand().equals(ControlCommand.PAUSE)) {
			try {
				// Block profile controller
				this.setAllowedToRun(false);
				this.wait();
			} catch (InterruptedException e) {
				log.trace("Wait interrupted client");
			}
		}
	}

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
			this.notify();
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
