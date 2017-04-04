package at.tuwien.ict.acona.cell.cellfunction;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public abstract class CellFunctionExecutorImpl extends CellFunctionImpl {

	private static final Logger log = LoggerFactory.getLogger(CellFunctionExecutorImpl.class);

	private int executeRate = 1000;
	private boolean executeOnce = true;

	protected ControlCommand currentCommand = ControlCommand.STOP;
	protected boolean runAllowed = false;

	protected abstract void executeFunction() throws Exception;

	protected abstract void executePostProcessing() throws Exception;

	protected abstract void executePreProcessing() throws Exception;

	public abstract void setCommand(ControlCommand command);

	@Override
	protected void cellFunctionInit() throws Exception {
		// Get execute once as optional
		if (this.getFunctionConfig().isExecuteOnce() != null) {
			this.setExecuteOnce(this.getFunctionConfig().isExecuteOnce().getAsBoolean());
		} else {
			this.getFunctionConfig().setExecuterate(executeRate);
		}

		// Get executerate as optional
		if (this.getFunctionConfig().getExecuteRate() != null) {
			this.setExecuteRate(this.getFunctionConfig().getExecuteRate().getAsInt());
		} else {
			this.getFunctionConfig().setExecuteOnce(executeOnce);
		}

		this.cellFunctionExecutorInit();

	}

	protected abstract void cellFunctionExecutorInit() throws Exception;

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

	public void setStart() {
		this.setCommand(ControlCommand.START);
	}

	public void setStop() {
		this.setCommand(ControlCommand.STOP);
	}

	public void setPause() {
		this.setCommand(ControlCommand.PAUSE);

	}

	@Override
	protected void shutDownImplementation() throws Exception {
		this.setCommand(ControlCommand.EXIT);
		this.shutDownExecutor();
	}

	protected abstract void shutDownExecutor() throws Exception;

	public int getExecuteRate() {
		return executeRate;
	}

	public void setExecuteRate(int blockingTime) {
		this.executeRate = blockingTime;
	}

	protected boolean isExecuteOnce() {
		return executeOnce;
	}

	protected void setExecuteOnce(boolean executeOnce) {
		this.executeOnce = executeOnce;
	}

	protected ControlCommand getCurrentCommand() {
		return currentCommand;
	}

	protected void setCurrentCommand(ControlCommand currentCommand) {
		this.currentCommand = currentCommand;
	}

	protected boolean isAllowedToRun() {
		return runAllowed;
	}

	protected void setAllowedToRun(boolean isAllowedToRun) {
		this.runAllowed = isAllowedToRun;
	}

}
