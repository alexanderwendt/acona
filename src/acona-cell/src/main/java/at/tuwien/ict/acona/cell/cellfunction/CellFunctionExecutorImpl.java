package at.tuwien.ict.acona.cell.cellfunction;

import java.util.Map;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public abstract class CellFunctionExecutorImpl extends CellFunctionImpl {

	private int executeRate = 1000;
	private boolean executeOnce = true;

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
	public void shutDown() {
		// Unsubscribe all datapoints
		// this.getCell().getFunctionHandler().deregisterActivatorInstance(this);

		// Execute specific functions
		this.getCell().getFunctionHandler().deregisterActivatorInstance(this);
		this.setCommand(ControlCommand.EXIT);
	}

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

}
