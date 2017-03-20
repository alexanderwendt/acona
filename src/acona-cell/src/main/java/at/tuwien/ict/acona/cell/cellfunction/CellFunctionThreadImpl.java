package at.tuwien.ict.acona.cell.cellfunction;

import java.util.Map;
import java.util.concurrent.SynchronousQueue;

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
public abstract class CellFunctionThreadImpl extends CellFunctionExecutorImpl implements Runnable {

	private static Logger log = LoggerFactory.getLogger(CellFunctionThreadImpl.class);

	private Thread t;

	private boolean isActive = true;

	/**
	 * The blocker is a queue, which is cleared at the start of the method and
	 * the value true is put at the end of the method. In that way, external
	 * applications can execute blocking functions with a non-blocking class.
	 */
	private final SynchronousQueue<Boolean> blocker = new SynchronousQueue<>();

	public CellFunctionThreadImpl() {

	}

	@Override
	protected void cellFunctionExecutorInit() throws Exception {
		try {
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
					//Clear the blocker queue
					//blocker.clear();
					executePreProcessing();

					executeFunction();

					executePostProcessing();

					//Add true to release the queue
					//blocker.(true);
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
					if (this.getCurrentCommand().equals(ControlCommand.EXIT) == false) {
						this.setCommand(ControlCommand.PAUSE.toString());
					}
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
		//		if (t != null) {
		//			while (!t.getState().equals(State.WAITING)) {
		//
		//			}
		//		}

		this.updateDatapointsByIdOnThread(data);
	}

	protected abstract void updateDatapointsByIdOnThread(Map<String, Datapoint> data);

	// === Internal functions for the control of the tread ===//

	/**
	 * Check, which command is valid and block until finished
	 */
	private synchronized void executeWait() {
		while (this.getCurrentCommand().equals(ControlCommand.STOP) == true || this.getCurrentCommand().equals(ControlCommand.PAUSE) == true) {
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
			this.notify();
		}
	}

	@Override
	protected void shutDownImplementation() {
		this.setCommand(ControlCommand.EXIT);

	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	protected SynchronousQueue<Boolean> getBlocker() {
		return blocker;
	}

}
